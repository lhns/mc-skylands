package org.lolhens.skylands.world.chunk

import net.minecraft.block.material.Material
import net.minecraft.block.{BlockFalling, BlockFlower}
import net.minecraft.init.{Biomes, Blocks}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.feature._
import net.minecraft.world.gen.{MapGenCaves, NoiseGeneratorOctaves, NoiseGeneratorPerlin}

import scala.util.Random

/**
  * Created by pierr on 05.01.2017.
  */
class SkylandsTerrainGenerator(world: World, random: Random) extends TerrainGenerator(world, random) {
  // Order is important!
  private val terrainNoise1 = new NoiseGeneratorOctaves(random.self, 16)
  private val terrainNoise2 = new NoiseGeneratorOctaves(random.self, 16)
  private val terrainNoise3 = new NoiseGeneratorOctaves(random.self, 8)
  new NoiseGeneratorOctaves(random.self, 4)
  private val biomeBlocksNoise = new NoiseGeneratorOctaves(random.self, 4)
  new NoiseGeneratorOctaves(random.self, 10)
  new NoiseGeneratorOctaves(random.self, 16)
  private val treeNoise = new NoiseGeneratorPerlin(random.self, 8)

  private val mapGenCaves = new MapGenCaves()

  override def generate(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val biomes: Array[Biome] = world.getBiomeProvider.getBiomes(new Array[Biome](0), chunkX * 16, chunkZ * 16, 16, 16)
    generateStone(chunkX, chunkZ, primer)
    replaceBiomeBlocks(chunkX, chunkZ, primer, biomes)
    mapGenCaves.generate(world, chunkX, chunkZ, primer)
  }

  private def getNoiseArray(xOffset: Int, yOffset: Int, zOffset: Int, xSize: Int, ySize: Int, zSize: Int): Array[Double] = {
    val noiseArray = new Array[Double](xSize * ySize * zSize)

    val hScale: Double = 684.41200000000003D * 2D
    val vScale: Double = 684.41200000000003D

    val terrainNoiseArray1: Array[Double] = terrainNoise1.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale, vScale, hScale)
    val terrainNoiseArray2: Array[Double] = terrainNoise2.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale, vScale, hScale)
    val terrainNoiseArray3: Array[Double] = terrainNoise3.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, hScale / 80D, vScale / 160D, hScale / 80D)

    var index: Int = 0
    for (_ <- 0 until xSize;
         _ <- 0 until zSize;
         z <- 0 until ySize) {
      val terrainNoiseValue1: Double = terrainNoiseArray1(index) / 512D
      val terrainNoiseValue2: Double = terrainNoiseArray2(index) / 512D
      val terrainNoiseValue3: Double = (terrainNoiseArray3(index) / 10D + 1.0D) / 2D

      var noise: Double =
        if (terrainNoiseValue3 < 0.0D)
          terrainNoiseValue1
        else if (terrainNoiseValue3 > 1.0D)
          terrainNoiseValue2
        else
          terrainNoiseValue1 + (terrainNoiseValue2 - terrainNoiseValue1) * terrainNoiseValue3

      noise -= 8D

      if (z > ySize - 32) {
        val multiplicator: Double = (z - (ySize - 32)).toFloat / (32.toFloat - 1.0F)
        noise = noise * (1.0D - multiplicator) + -30D * multiplicator
      }
      if (z < 8) {
        val multiplicator: Double = (8 - z).toFloat / (8.toFloat - 1.0F)
        noise = noise * (1.0D - multiplicator) + -30D * multiplicator
      }
      noiseArray(index) = noise
      index += 1
    }

    noiseArray
  }

  private def generateStone(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val noiseArray: Array[Double] = getNoiseArray(chunkX * 2, 0, chunkZ * 2, 3, 33, 3)

    for (i1 <- 0 until 2;
         j1 <- 0 until 2;
         k1 <- 0 until 32) {
      var noiseOffset1: Double = noiseArray((i1 * 3 + j1) * 33 + k1)
      var noiseOffset2: Double = noiseArray((i1 * 3 + (j1 + 1)) * 33 + k1)
      var noiseOffset3: Double = noiseArray(((i1 + 1) * 3 + j1) * 33 + k1)
      var noiseOffset4: Double = noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + k1)

      val noiseIncreaseScale: Double = 0.25D
      val noiseIncrease1: Double = (noiseArray((i1 * 3 + j1) * 33 + (k1 + 1)) - noiseOffset1) * noiseIncreaseScale
      val noiseIncrease2: Double = (noiseArray((i1 * 3 + (j1 + 1)) * 33 + (k1 + 1)) - noiseOffset2) * noiseIncreaseScale
      val noiseIncrease3: Double = (noiseArray(((i1 + 1) * 3 + j1) * 33 + (k1 + 1)) - noiseOffset3) * noiseIncreaseScale
      val noiseIncrease4: Double = (noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + (k1 + 1)) - noiseOffset4) * noiseIncreaseScale

      for (l1 <- 0 until 4) {
        val noiseIncrease2Scale: Double = 0.125D
        var noiseValue1: Double = noiseOffset1
        var noiseValue2: Double = noiseOffset2

        val noiseIncrease21: Double = (noiseOffset3 - noiseOffset1) * noiseIncrease2Scale
        val noiseIncrease22: Double = (noiseOffset4 - noiseOffset2) * noiseIncrease2Scale

        for (i2 <- 0 until 8) {
          var x: Int = j1 * 8
          val y: Int = k1 * 4 + l1
          val z: Int = i2 + i1 * 8

          val terrainDensityIncreaseScale: Double = 0.125D
          var terrainDensity: Double = noiseValue1
          val terrainDensityIncrease: Double = (noiseValue2 - noiseValue1) * terrainDensityIncreaseScale

          for (_ <- 0 until 8) {
            primer.setBlockState(z, y, x,
              if (terrainDensity > 0.0D)
                Blocks.STONE.getDefaultState
              else
                Blocks.AIR.getDefaultState
            )

            x += 1
            terrainDensity += terrainDensityIncrease
          }

          noiseValue1 += noiseIncrease21
          noiseValue2 += noiseIncrease22
        }

        noiseOffset1 += noiseIncrease1
        noiseOffset2 += noiseIncrease2
        noiseOffset3 += noiseIncrease3
        noiseOffset4 += noiseIncrease4
      }
    }
  }

  private def replaceBiomeBlocks(xChunk: Int, zChunk: Int, primer: ChunkPrimer, biomes: Array[Biome]): Unit = {
    val scale: Double = 0.03125D

    val biomeBlocksNoiseArray: Array[Double] = biomeBlocksNoise.generateNoiseOctaves(new Array[Double](256), xChunk * 16, zChunk * 16, 0, 16, 16, 1, scale * 2D, scale * 2D, scale * 2D)

    for (x <- 0 until 16;
         z <- 0 until 16) {
      val biome = biomes(x + z * 16)
      val biomeBlocksNoiseValue: Int = (biomeBlocksNoiseArray(x + z * 16) / 3D + 3D + random.nextDouble() * 0.25D).toInt

      var biomeBlocksLeft = -1

      var topBlock = biome.topBlock
      var fillerBlock = biome.fillerBlock

      for (y <- 127 to 0 by -1) {
        val block = primer.getBlockState(x, y, z).getBlock
        if (block == Blocks.AIR)
          biomeBlocksLeft = -1
        else if (block == Blocks.STONE) {
          if (biomeBlocksLeft == -1) {
            if (biomeBlocksNoiseValue <= 0) {
              topBlock = Blocks.AIR.getDefaultState
              fillerBlock = Blocks.STONE.getDefaultState
            }

            biomeBlocksLeft = biomeBlocksNoiseValue

            primer.setBlockState(x, y, z, if (y >= 0) topBlock else fillerBlock)
          } else if (biomeBlocksLeft > 0) {
            biomeBlocksLeft -= 1

            primer.setBlockState(x, y, z, fillerBlock)

            if (biomeBlocksLeft == 0 && fillerBlock.getBlock == Blocks.SAND) {
              biomeBlocksLeft = random.nextInt(4)
              fillerBlock = Blocks.SANDSTONE.getDefaultState
            }
          }
        }
      }

    }

  }

  override def populate(chunkX: Int, chunkZ: Int): Unit = {
    BlockFalling.fallInstantly = true

    val chunkWorldPos = new BlockPos(
      chunkX * 16,
      0,
      chunkZ * 16
    )

    val biome = world.getBiome(chunkWorldPos.add(16, 0, 16))

    val populateRandom = new Random(world.getSeed)
    populateRandom.setSeed(
      chunkX.toLong * ((populateRandom.nextLong() / 2L) * 2L + 1L) +
        chunkZ.toLong * ((populateRandom.nextLong() / 2L) * 2L + 1L) ^ world.getSeed
    )

    if (populateRandom.nextInt(4) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenLakes(Blocks.WATER).generate(world, populateRandom.self, pos)
    }

    if (populateRandom.nextInt(8) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(populateRandom.nextInt(120) + 8),
        populateRandom.nextInt(16) + 8
      )

      if (pos.getY < 64 || populateRandom.nextInt(10) == 0)
        new WorldGenLakes(Blocks.LAVA).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 8) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenDungeons().generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 10) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(128),
        populateRandom.nextInt(16)
      )

      new WorldGenClay(32).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 20) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(128),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.DIRT.getDefaultState, 32).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 10) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(128),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.GRAVEL.getDefaultState, 32).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 20) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(128),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.COAL_ORE.getDefaultState, 16).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 20) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(64),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.IRON_ORE.getDefaultState, 8).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 2) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(32),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState, 8).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 8) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(16),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState, 7).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 1) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(16),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState, 7).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 1) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16),
        populateRandom.nextInt(16) + populateRandom.nextInt(16),
        populateRandom.nextInt(16)
      )

      new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState, 6).generate(world, populateRandom.self, pos)
    }

    val treeNoiseValue = ((treeNoise.getValue(chunkWorldPos.getX.toDouble * 0.5D, chunkWorldPos.getZ.toDouble * 0.5D) / 8D + populateRandom.nextDouble() * 4D + 4D) / 3D).toInt

    var numTrees = 0

    if (populateRandom.nextInt(10) == 0) numTrees += 1

    if (biome == Biomes.FOREST) numTrees += treeNoiseValue + 5

    // rainforest
    if (biome == Biomes.FOREST_HILLS) numTrees += treeNoiseValue + 5

    // seasonalForest
    if (biome == Biomes.BIRCH_FOREST) numTrees += treeNoiseValue + 2

    if (biome == Biomes.TAIGA) numTrees += treeNoiseValue + 5

    if (biome == Biomes.DESERT) numTrees -= 20

    // tundra
    if (biome == Biomes.COLD_TAIGA) numTrees -= 20

    if (biome == Biomes.PLAINS) numTrees -= 20

    for (_ <- 0 until numTrees) {
      val xzPos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        0,
        populateRandom.nextInt(16) + 8
      )

      val pos = xzPos.add(0, world.getHeightmapHeight(xzPos.getX, xzPos.getZ), 0)

      val treeGenerator = biome.genBigTreeChance(populateRandom.self)
      treeGenerator.generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 2) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION).generate(world, populateRandom.self, pos)
    }

    if (populateRandom.nextInt(2) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenFlowers(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY).generate(world, populateRandom.self, pos)
    }
    if (populateRandom.nextInt(4) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenBush(Blocks.BROWN_MUSHROOM).generate(world, populateRandom.self, pos)
    }
    if (populateRandom.nextInt(8) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenBush(Blocks.RED_MUSHROOM).generate(world, populateRandom.self, pos)
    }
    for (_ <- 0 until 10) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenReed().generate(world, populateRandom.self, pos)
    }

    if (populateRandom.nextInt(32) == 0) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenPumpkin().generate(world, populateRandom.self, pos)
    }


    for (_ <- 0 until (if (biome == Biomes.DESERT) 10 else 0)) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(128),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenCactus().generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 50) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(populateRandom.nextInt(120) + 8),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenLiquids(Blocks.FLOWING_WATER).generate(world, populateRandom.self, pos)
    }

    for (_ <- 0 until 20) {
      val pos = chunkWorldPos.add(
        populateRandom.nextInt(16) + 8,
        populateRandom.nextInt(populateRandom.nextInt(populateRandom.nextInt(112) + 8) + 8),
        populateRandom.nextInt(16) + 8
      )

      new WorldGenLiquids(Blocks.FLOWING_LAVA).generate(world, populateRandom.self, pos)
    }

    for (x2 <- 0 until 16;
         z2 <- 0 until 16) {
      val xzPos = chunkWorldPos.add(x2 + 8, 0, z2 + 8)
      val snowPos = world.getTopSolidOrLiquidBlock(xzPos)

      if (world.canSnowAt(snowPos, true) && snowPos.getY > 0 && snowPos.getY < 128 &&
        world.isAirBlock(snowPos) &&
        world.getBlockState(snowPos.add(0, -1, 0)).getMaterial.isSolid &&
        world.getBlockState(snowPos.add(0, -1, 0)).getMaterial != Material.ICE) {
        world.setBlockState(snowPos, Blocks.SNOW_LAYER.getDefaultState)
      }
    }

    BlockFalling.fallInstantly = false
  }
}
