package skylands.worldgen

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.{BiomeManager, BiomeSource}
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.{ChunkAccess, ChunkGenerator}
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.synth.ImprovedNoise
import net.minecraft.world.level.levelgen.{GenerationStep, Heightmap, LegacyRandomSource, RandomState}
import net.minecraft.world.level.{LevelHeightAccessor, NoiseColumn, StructureManager}
import skylands.worldgen.noise.OctaveNoise

import java.util.concurrent.{CompletableFuture, ConcurrentHashMap}

// Faithful port of 1.12.2 SkylandsTerrainGenerator to 1.21.1.
//
// Maps 1.12.2's primer coordinates (offset by 64 on Y) to world coordinates
// y=SkylandsChunkGenerator.TerrainBottomY..TerrainTopY. Same 3x33x3 noise
// buckets, same octaves, same scales, same biome surface replacement.
class SkylandsChunkGenerator(biomeSource: BiomeSource) extends ChunkGenerator(biomeSource):
  import SkylandsChunkGenerator._

  override def codec(): MapCodec[? <: ChunkGenerator] = SkylandsChunkGenerator.CODEC

  override def applyCarvers(
      region: WorldGenRegion,
      seed: Long,
      randomState: RandomState,
      biomeManager: BiomeManager,
      structureManager: StructureManager,
      chunk: ChunkAccess,
      step: GenerationStep.Carving
  ): Unit = ()

  override def buildSurface(
      region: WorldGenRegion,
      structureManager: StructureManager,
      randomState: RandomState,
      chunk: ChunkAccess
  ): Unit =
    getOrCreateNoise(randomState).replaceBiomeBlocks(chunk)

  override def spawnOriginalMobs(region: WorldGenRegion): Unit = ()

  override def getGenDepth(): Int = 384

  override def fillFromNoise(
      blender: Blender,
      randomState: RandomState,
      structureManager: StructureManager,
      chunk: ChunkAccess
  ): CompletableFuture[ChunkAccess] =
    val ctx = getOrCreateNoise(randomState)
    CompletableFuture.supplyAsync(() => {
      ctx.generateStone(chunk)
      chunk
    })

  override def getSeaLevel(): Int = -64
  override def getMinY(): Int = -64

  override def getBaseHeight(
      x: Int,
      z: Int,
      heightmapType: Heightmap.Types,
      level: LevelHeightAccessor,
      randomState: RandomState
  ): Int = TerrainBottomY

  override def getBaseColumn(
      x: Int,
      z: Int,
      level: LevelHeightAccessor,
      randomState: RandomState
  ): NoiseColumn =
    new NoiseColumn(level.getMinBuildHeight, Array.empty)

  override def addDebugScreenInfo(
      info: java.util.List[String],
      randomState: RandomState,
      pos: BlockPos
  ): Unit = ()

  // Cached per world seed so every chunk in the same world sees the same noise state,
  // without needing a mutable external setter. The seed is derived from a stable
  // PositionalRandomFactory anchored at (0,0,0) keyed by our mod id; this gives a
  // deterministic per-world value we can use as the 1.12.2-style legacy seed.
  private val noiseCache: ConcurrentHashMap[java.lang.Long, NoiseContext] = new ConcurrentHashMap()

  private def getOrCreateNoise(randomState: RandomState): NoiseContext =
    val factory = randomState.getOrCreateRandomFactory(skylands.registry.SkylandsWorldgen.SKYLANDS_ID)
    val derivedSeed: Long = factory.at(0, 0, 0).nextLong()
    noiseCache.computeIfAbsent(java.lang.Long.valueOf(derivedSeed), k => new NoiseContext(k.longValue))

object SkylandsChunkGenerator:
  // 1.12.2 SkylandsTerrainGenerator wrote to primer y=0..127 with OffsetChunkPrimer(offset=64),
  // producing world terrain in y=64..191. We keep the same range for visual fidelity.
  final val TerrainBottomY: Int = 64
  final val TerrainTopY: Int = 192 // exclusive
  final val TerrainHeight: Int = TerrainTopY - TerrainBottomY // 128

  val CODEC: MapCodec[SkylandsChunkGenerator] =
    RecordCodecBuilder.mapCodec(instance =>
      instance
        .group(
          BiomeSource.CODEC.fieldOf("biome_source").forGetter((g: SkylandsChunkGenerator) => g.getBiomeSource)
        )
        .apply(instance, new SkylandsChunkGenerator(_))
    )

  // Holds the five noise instances the 1.12.2 generator used, seeded in the original order
  // from a single LCG derived from the world seed (matches the 1.12.2 formula
  // `new Random(world.getSeed * 0x4f9939f508L)`).
  final class NoiseContext(seed: Long):
    private val ctxRandom: RandomSource = new LegacyRandomSource(seed * 0x4f9939f508L)
    // Order matters — 1.12.2 instantiated the octaves in this exact sequence so the shared
    // Random advances through the same draws.
    private val terrainNoise1 = new OctaveNoise(ctxRandom, 16)
    private val terrainNoise2 = new OctaveNoise(ctxRandom, 16)
    private val terrainNoise3 = new OctaveNoise(ctxRandom, 8)
    private val _unusedA = new OctaveNoise(ctxRandom, 4)
    private val biomeBlocksNoise = new OctaveNoise(ctxRandom, 4)
    private val _unusedB = new OctaveNoise(ctxRandom, 10)
    private val _unusedC = new OctaveNoise(ctxRandom, 16)

    // Consume the final NoiseGeneratorPerlin(random, 8) draws that 1.12.2 did for treeNoise.
    // We don't use tree density noise yet, but keeping the random-state footprint means any
    // future populate pass sees the same sequence the original did.
    private val _unusedTreeNoise = Array.fill(8)(new ImprovedNoise(ctxRandom))

    // ---- terrain stone fill ---------------------------------------------------
    def generateStone(chunk: ChunkAccess): Unit =
      val chunkX = chunk.getPos.x
      val chunkZ = chunk.getPos.z
      val noiseArray = getNoiseArray(chunkX * 2, 0, chunkZ * 2, 3, 33, 3)

      val stone: BlockState = Blocks.STONE.defaultBlockState()
      val pos = new BlockPos.MutableBlockPos()

      var i1 = 0
      while i1 < 2 do
        var j1 = 0
        while j1 < 2 do
          var k1 = 0
          while k1 < 32 do
            var noiseOffset1 = noiseArray((i1 * 3 + j1) * 33 + k1)
            var noiseOffset2 = noiseArray((i1 * 3 + (j1 + 1)) * 33 + k1)
            var noiseOffset3 = noiseArray(((i1 + 1) * 3 + j1) * 33 + k1)
            var noiseOffset4 = noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + k1)

            val noiseIncreaseScale = 0.25
            val noiseIncrease1 = (noiseArray((i1 * 3 + j1) * 33 + (k1 + 1)) - noiseOffset1) * noiseIncreaseScale
            val noiseIncrease2 = (noiseArray((i1 * 3 + (j1 + 1)) * 33 + (k1 + 1)) - noiseOffset2) * noiseIncreaseScale
            val noiseIncrease3 = (noiseArray(((i1 + 1) * 3 + j1) * 33 + (k1 + 1)) - noiseOffset3) * noiseIncreaseScale
            val noiseIncrease4 = (noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + (k1 + 1)) - noiseOffset4) * noiseIncreaseScale

            var l1 = 0
            while l1 < 4 do
              val noiseIncrease2Scale = 0.125
              var noiseValue1 = noiseOffset1
              var noiseValue2 = noiseOffset2

              val noiseIncrease21 = (noiseOffset3 - noiseOffset1) * noiseIncrease2Scale
              val noiseIncrease22 = (noiseOffset4 - noiseOffset2) * noiseIncrease2Scale

              var i2 = 0
              while i2 < 8 do
                val baseX = j1 * 8
                val yPrimer = k1 * 4 + l1
                val zPrimer = i2 + i1 * 8

                val terrainDensityIncreaseScale = 0.125
                var terrainDensity = noiseValue1
                val terrainDensityIncrease = (noiseValue2 - noiseValue1) * terrainDensityIncreaseScale

                var n = 0
                while n < 8 do
                  // 1.12.2 passes primer.setBlockState(z, y, x, state) — preserve the transpose.
                  val px = zPrimer
                  val pz = baseX + n
                  val worldY = TerrainBottomY + yPrimer
                  if terrainDensity > 0.0 && worldY >= TerrainBottomY && worldY < TerrainTopY then
                    pos.set(chunkX * 16 + px, worldY, chunkZ * 16 + pz)
                    chunk.setBlockState(pos, stone, false)
                  terrainDensity += terrainDensityIncrease
                  n += 1

                noiseValue1 += noiseIncrease21
                noiseValue2 += noiseIncrease22
                i2 += 1

              noiseOffset1 += noiseIncrease1
              noiseOffset2 += noiseIncrease2
              noiseOffset3 += noiseIncrease3
              noiseOffset4 += noiseIncrease4
              l1 += 1

            k1 += 1
          j1 += 1
        i1 += 1

    // ---- biome surface replacement -------------------------------------------
    def replaceBiomeBlocks(chunk: ChunkAccess): Unit =
      val chunkX = chunk.getPos.x
      val chunkZ = chunk.getPos.z
      // Fresh per-chunk random so concurrent chunk gen stays deterministic.
      val chunkRandom: RandomSource =
        new LegacyRandomSource((chunkX.toLong * 341873128712L + chunkZ.toLong * 132897987541L) ^ seed)
      val scale = 0.03125
      val biomeBlocksNoiseArray =
        biomeBlocksNoise.generateNoiseOctaves(
          new Array[Double](256),
          chunkX * 16, chunkZ * 16, 0,
          16, 16, 1,
          scale * 2.0, scale * 2.0, scale * 2.0
        )

      val grass = Blocks.GRASS_BLOCK.defaultBlockState()
      val dirt = Blocks.DIRT.defaultBlockState()
      val sand = Blocks.SAND.defaultBlockState()
      val sandstone = Blocks.SANDSTONE.defaultBlockState()
      val stone = Blocks.STONE.defaultBlockState()
      val air = Blocks.AIR.defaultBlockState()

      val pos = new BlockPos.MutableBlockPos()

      var x = 0
      while x < 16 do
        var z = 0
        while z < 16 do
          val biomeNoise = (biomeBlocksNoiseArray(x + z * 16) / 3.0 + 3.0 + chunkRandom.nextDouble() * 0.25).toInt

          var biomeBlocksLeft = -1
          var topBlock: BlockState = grass
          var fillerBlock: BlockState = dirt

          var y = TerrainTopY - 1
          while y >= TerrainBottomY do
            pos.set(chunkX * 16 + x, y, chunkZ * 16 + z)
            val state = chunk.getBlockState(pos)
            val isAir = state.isAir
            val isStone = state.is(Blocks.STONE)

            if isAir then
              biomeBlocksLeft = -1
            else if isStone then
              if biomeBlocksLeft == -1 then
                if biomeNoise <= 0 then
                  topBlock = air
                  fillerBlock = stone
                biomeBlocksLeft = biomeNoise
                chunk.setBlockState(pos, if y >= TerrainBottomY then topBlock else fillerBlock, false)
              else if biomeBlocksLeft > 0 then
                biomeBlocksLeft -= 1
                chunk.setBlockState(pos, fillerBlock, false)
                if biomeBlocksLeft == 0 && fillerBlock == sand then
                  biomeBlocksLeft = chunkRandom.nextInt(4)
                  fillerBlock = sandstone
            y -= 1

          z += 1
        x += 1

    // ---- noise sampling ------------------------------------------------------
    private def getNoiseArray(xOffset: Int, yOffset: Int, zOffset: Int, xSize: Int, ySize: Int, zSize: Int): Array[Double] =
      val result = new Array[Double](xSize * ySize * zSize)

      val hScale = 684.41200000000003 * 2.0
      val vScale = 684.41200000000003

      val n1 = terrainNoise1.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale, vScale, hScale)
      val n2 = terrainNoise2.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale, vScale, hScale)
      val n3 = terrainNoise3.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale / 80.0, vScale / 160.0, hScale / 80.0)

      var index = 0
      var x = 0
      while x < xSize do
        var z = 0
        while z < zSize do
          var zy = 0
          while zy < ySize do
            val v1 = n1(index) / 512.0
            val v2 = n2(index) / 512.0
            val v3 = (n3(index) / 10.0 + 1.0) / 2.0

            var noise =
              if v3 < 0.0 then v1
              else if v3 > 1.0 then v2
              else v1 + (v2 - v1) * v3

            noise -= 8.0

            if zy > ySize - 32 then
              val mult = (zy - (ySize - 32)).toFloat / (32.toFloat - 1.0f)
              noise = noise * (1.0 - mult) + -30.0 * mult
            if zy < 8 then
              val mult = (8 - zy).toFloat / (8.toFloat - 1.0f)
              noise = noise * (1.0 - mult) + -30.0 * mult

            result(index) = noise
            index += 1
            zy += 1
          z += 1
        x += 1
      result
