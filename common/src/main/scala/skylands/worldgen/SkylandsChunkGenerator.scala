package skylands.worldgen

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.{LevelHeightAccessor, NoiseColumn, StructureManager}
import net.minecraft.world.level.biome.{BiomeManager, BiomeSource}
import net.minecraft.world.level.chunk.{ChunkAccess, ChunkGenerator}
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.{GenerationStep, Heightmap, RandomState}

import java.util.concurrent.CompletableFuture

class SkylandsChunkGenerator(biomeSource: BiomeSource) extends ChunkGenerator(biomeSource):
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
  ): Unit = ()

  override def spawnOriginalMobs(region: WorldGenRegion): Unit = ()

  override def getGenDepth(): Int = 384

  override def fillFromNoise(
      blender: Blender,
      randomState: RandomState,
      structureManager: StructureManager,
      chunk: ChunkAccess
  ): CompletableFuture[ChunkAccess] =
    CompletableFuture.completedFuture(chunk)

  override def getSeaLevel(): Int = -64

  override def getMinY(): Int = -64

  override def getBaseHeight(
      x: Int,
      z: Int,
      heightmapType: Heightmap.Types,
      level: LevelHeightAccessor,
      randomState: RandomState
  ): Int = level.getMinBuildHeight

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

object SkylandsChunkGenerator:
  val CODEC: MapCodec[SkylandsChunkGenerator] =
    RecordCodecBuilder.mapCodec(instance =>
      instance
        .group(
          BiomeSource.CODEC.fieldOf("biome_source").forGetter((g: SkylandsChunkGenerator) => g.getBiomeSource)
        )
        .apply(instance, new SkylandsChunkGenerator(_))
    )
