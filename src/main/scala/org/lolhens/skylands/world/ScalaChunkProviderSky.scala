package org.lolhens.skylands.world

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
  * Created by pierr on 09.01.2017.
  */
class ScalaChunkProviderSky(world: World, random: Random) {
  private val noise10 = new NoiseGeneratorOctaves(random.self, 4)
  private val noise11 = new NoiseGeneratorOctaves(random.self, 4)
  private val noise12 = new NoiseGeneratorPerlin(random.self, 8)
  private val noiseArray = null
  private val mapGenCaves = new MapGenCaves()


  private val lperlinNoise1 = new NoiseGeneratorOctaves(random.self, 16)
  private val lperlinNoise2 = new NoiseGeneratorOctaves(random.self, 16)
  private val perlinNoise1 = new NoiseGeneratorOctaves(random.self, 8)

  private def getNoiseArray(xOffset: Int, yOffset: Int, zOffset: Int, xSize: Int, ySize: Int, zSize: Int): Array[Double] = {
    val noiseArray = new Array[Double](xSize * ySize * zSize)
    val d: Double = 684.41200000000003D * 2
    val d1: Double = 684.41200000000003D

    val noise1: Array[Double] = perlinNoise1.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, d / 80D, d1 / 160D, d / 80D)
    val noisel1: Array[Double] = lperlinNoise1.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, d, d1, d)
    val noise2: Array[Double] = lperlinNoise2.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, d, d1, d)
    var index = 0
    for (_ <- 0 until xSize;
         _ <- 0 until zSize;
         z <- 0 until ySize) {
      val d10: Double = noisel1(index) / 512D
      val d11: Double = noise2(index) / 512D
      val d12: Double = (noise1(index) / 10D + 1.0D) / 2D

      var noise: Double =
        if (d12 < 0.0D)
          d10
        else if (d12 > 1.0D)
          d11
        else
          d10 + (d11 - d10) * d12

      noise -= 8

      if (z > ySize - 32) {
        val d13: Double = (z - (ySize - 32)).toFloat / (32 - 1.0F)
        noise = noise * (1.0D - d13) + -30D * d13
      }
      if (z < 8) {
        val d14: Double = (8 - z).toFloat / (8 - 1.0F)
        noise = noise * (1.0D - d14) + -30D * d14
      }
      noiseArray(index) = noise
      index += 1
    }

    noiseArray
  }

  def generateStone(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val noiseArray: Array[Double] = getNoiseArray(chunkX * 2, 0, chunkZ * 2, 3, 33, 3)
    for (i1 <- 0 until 2;
         j1 <- 0 until 2;
         k1 <- 0 until 32) {
      val d: Double = 0.25D
      var d1: Double = noiseArray((i1 * 3 + j1) * 33 + k1)
      var d2: Double = noiseArray((i1 * 3 + (j1 + 1)) * 33 + k1)
      var d3: Double = noiseArray(((i1 + 1) * 3 + j1) * 33 + k1)
      var d4: Double = noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + k1)
      val d5: Double = (noiseArray((i1 * 3 + j1) * 33 + (k1 + 1)) - d1) * d
      val d6: Double = (noiseArray((i1 * 3 + (j1 + 1)) * 33 + (k1 + 1)) - d2) * d
      val d7: Double = (noiseArray(((i1 + 1) * 3 + j1) * 33 + (k1 + 1)) - d3) * d
      val d8: Double = (noiseArray(((i1 + 1) * 3 + (j1 + 1)) * 33 + (k1 + 1)) - d4) * d
      for (l1 <- 0 until 4) {
        val d9: Double = 0.125D
        var d10: Double = d1
        var d11: Double = d2
        val d12: Double = (d3 - d1) * d9
        val d13: Double = (d4 - d2) * d9
        for (i2 <- 0 until 8) {
          var x: Int = j1 * 8
          val y: Int = k1 * 4 + l1
          val z: Int = i2 + i1 * 8
          val d14: Double = 0.125D
          var d15: Double = d10
          val d16: Double = (d11 - d10) * d14
          for (_ <- 0 until 8) {
            primer.setBlockState(z, y, x,
              if (d15 > 0.0D)
                Blocks.STONE.getDefaultState
              else
                Blocks.AIR.getDefaultState
            )

            x += 1
            d15 += d16
          }
          d10 += d12
          d11 += d13
        }
        d1 += d5
        d2 += d6
        d3 += d7
        d4 += d8
      }
    }
  }

  def replaceBiomeBlocks(xChunk: Int, zChunk: Int, primer: ChunkPrimer, biomes: Array[Biome]): Unit = {
    val d: Double = 0.03125D
    val br: Array[Double] = noise11.generateNoiseOctaves(new Array[Double](256), xChunk * 16, zChunk * 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D)
    for (x <- 0 until 16;
         z <- 0 until 16) {
      val biome = biomes(x + z * 16)
      val i1: Int = (br(x + z * 16) / 3D + 3D + random.nextDouble() * 0.25D).toInt
      var j1 = -1
      var topBlock = biome.topBlock
      var fillerBlock = biome.fillerBlock
      for (y <- 127 to 0 by -1) {
        val block = primer.getBlockState(x, y, z).getBlock
        if (block == Blocks.AIR) {
          j1 = -1
        } else if (block == Blocks.STONE) {
          if (j1 == -1) {
            if (i1 <= 0) {
              topBlock = Blocks.AIR.getDefaultState
              fillerBlock = Blocks.STONE.getDefaultState
            }
            j1 = i1
            primer.setBlockState(x, y, z, if (y >= 0) topBlock else fillerBlock)
          } else if (j1 > 0) {
            j1 -= 1
            primer.setBlockState(x, y, z, fillerBlock)
            if (j1 == 0 && fillerBlock.getBlock == Blocks.SAND) {
              j1 = random.nextInt(4)
              fillerBlock = Blocks.SANDSTONE.getDefaultState
            }
          }
        }
      }

    }

  }

  def provideChunk(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val biomes: Array[Biome] = world.getBiomeProvider.getBiomes(new Array[Biome](0), chunkX * 16, chunkZ * 16, 16, 16)
    generateStone(chunkX, chunkZ, primer)
    replaceBiomeBlocks(chunkX, chunkZ, primer, biomes)
    mapGenCaves.generate(world, chunkX, chunkZ, primer)
  }

  def populate(chunkX: Int, chunkZ: Int): Unit = {
    BlockFalling.fallInstantly = true

    val x = chunkX * 16
    val z = chunkZ * 16
    val biome = world.getBiome(new BlockPos(x + 16, 0, z + 16))
    random.setSeed(world.getSeed)
    val l1: Long = (random.nextLong() / 2L) * 2L + 1L
    val l2: Long = (random.nextLong() / 2L) * 2L + 1L
    random.setSeed(chunkX.toLong * l1 + chunkZ.toLong * l2 ^ world.getSeed)

    if (random.nextInt(4) == 0) {
      val i1 = x + random.nextInt(16) + 8
      val l4 = random.nextInt(128)
      val i8 = z + random.nextInt(16) + 8
      new WorldGenLakes(Blocks.WATER).generate(world, random.self, new BlockPos(i1, l4, i8))
    }
    if (random.nextInt(8) == 0) {
      val j1 = x + random.nextInt(16) + 8
      val i5 = random.nextInt(random.nextInt(120) + 8)
      val j8 = z + random.nextInt(16) + 8
      if (i5 < 64 || random.nextInt(10) == 0) {
        new WorldGenLakes(Blocks.LAVA).generate(world, random.self, new BlockPos(j1, i5, j8))
      }
    }
    for (k1 <- 0 until 8) {
      val j5 = x + random.nextInt(16) + 8
      val k8 = random.nextInt(128)
      val i13 = z + random.nextInt(16) + 8
      new WorldGenDungeons().generate(world, random.self, new BlockPos(j5, k8, i13))
    }

    for (i2 <- 0 until 10) {
      val k5 = x + random.nextInt(16)
      val l8 = random.nextInt(128)
      val j13 = z + random.nextInt(16)
      new WorldGenClay(32).generate(world, random.self, new BlockPos(k5, l8, j13))
    }

    for (j2 <- 0 until 20) {
      val l5 = x + random.nextInt(16)
      val i9 = random.nextInt(128)
      val k13 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.DIRT.getDefaultState, 32).generate(world, random.self, new BlockPos(l5, i9, k13))
    }

    for (k2 <- 0 until 10) {
      val i6 = x + random.nextInt(16)
      val j9 = random.nextInt(128)
      val l13 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.GRAVEL.getDefaultState, 32).generate(world, random.self, new BlockPos(i6, j9, l13))
    }

    for (i <- 0 until 20) {
      val j6 = x + random.nextInt(16)
      val k9 = random.nextInt(128)
      val i14 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.COAL_ORE.getDefaultState, 16).generate(world, random.self, new BlockPos(j6, k9, i14))
    }

    for (j3 <- 0 until 20) {
      val k6 = x + random.nextInt(16)
      val l9 = random.nextInt(64)
      val j14 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.IRON_ORE.getDefaultState, 8).generate(world, random.self, new BlockPos(k6, l9, j14))
    }

    for (k3 <- 0 until 2) {
      val l6 = x + random.nextInt(16)
      val i10 = random.nextInt(32)
      val k14 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState, 8).generate(world, random.self, new BlockPos(l6, i10, k14))
    }

    for (l3 <- 0 until 8) {
      val i7 = x + random.nextInt(16)
      val j10 = random.nextInt(16)
      val l14 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState, 7).generate(world, random.self, new BlockPos(i7, j10, l14))
    }

    for (i4 <- 0 until 1) {
      val j7 = x + random.nextInt(16)
      val k10 = random.nextInt(16)
      val i15 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState, 7).generate(world, random.self, new BlockPos(j7, k10, i15))
    }

    for (j4 <- 0 until 1) {
      val k7 = x + random.nextInt(16)
      val l10 = random.nextInt(16) + random.nextInt(16)
      val j15 = z + random.nextInt(16)
      new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState, 6).generate(world, random.self, new BlockPos(k7, l10, j15))
    }

    val k4 = ((noise12.getValue(x.toDouble * 0.5D, z.toDouble * 0.5D) / 8D + random.nextDouble() * 4D + 4D) / 3D).toInt

    var numTrees = 0
    if (random.nextInt(10) == 0) numTrees += 1
    if (biome == Biomes.FOREST) numTrees += k4 + 5
    // rainforest
    if (biome == Biomes.FOREST_HILLS) numTrees += k4 + 5
    // seasonalForest
    if (biome == Biomes.BIRCH_FOREST) numTrees += k4 + 2
    if (biome == Biomes.TAIGA) numTrees += k4 + 5
    if (biome == Biomes.DESERT) numTrees -= 20
    // tundra
    if (biome == Biomes.COLD_TAIGA) numTrees -= 20
    if (biome == Biomes.PLAINS) numTrees -= 20
    for (i11 <- 0 until numTrees) {
      val k15 = x + random.nextInt(16) + 8
      val j18 = z + random.nextInt(16) + 8
      val worldgenerator = biome.genBigTreeChance(random.self)
      worldgenerator.generate(world, random.self, new BlockPos(k15, world.getHeightmapHeight(k15, j18), j18))
    }

    for (j11 <- 0 until 2) {
      val l15 = x + random.nextInt(16) + 8
      val k18 = random.nextInt(128)
      val i21 = z + random.nextInt(16) + 8
      new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION).generate(world, random.self, new BlockPos(l15, k18, i21))
    }

    if (random.nextInt(2) == 0) {
      val x1 = x + random.nextInt(16) + 8
      val y1 = random.nextInt(128)
      val z1 = z + random.nextInt(16) + 8
      new WorldGenFlowers(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY).generate(world, random.self, new BlockPos(x1, y1, z1))
    }
    if (random.nextInt(4) == 0) {
      val l11 = x + random.nextInt(16) + 8
      val j16 = random.nextInt(128)
      val i19 = z + random.nextInt(16) + 8
      new WorldGenBush(Blocks.BROWN_MUSHROOM).generate(world, random.self, new BlockPos(l11, j16, i19))
    }
    if (random.nextInt(8) == 0) {
      val i12 = x + random.nextInt(16) + 8
      val k16 = random.nextInt(128)
      val j19 = z + random.nextInt(16) + 8
      new WorldGenBush(Blocks.RED_MUSHROOM).generate(world, random.self, new BlockPos(i12, k16, j19))
    }
    for (j12 <- 0 until 10) {
      val l16 = x + random.nextInt(16) + 8
      val k19 = random.nextInt(128)
      val j21 = z + random.nextInt(16) + 8
      new WorldGenReed().generate(world, random.self, new BlockPos(l16, k19, j21))
    }

    if (random.nextInt(32) == 0) {
      val k12 = x + random.nextInt(16) + 8
      val i17 = random.nextInt(128)
      val l19 = z + random.nextInt(16) + 8
      new WorldGenPumpkin().generate(world, random.self, new BlockPos(k12, i17, l19))
    }


    for (j17 <- 0 until (if (biome == Biomes.DESERT) 10 else 0)) {
      val i20 = x + random.nextInt(16) + 8
      val k21 = random.nextInt(128)
      val k22 = z + random.nextInt(16) + 8
      new WorldGenCactus().generate(world, random.self, new BlockPos(i20, k21, k22))
    }

    for (k17 <- 0 until 50) {
      val j20 = x + random.nextInt(16) + 8
      val l21 = random.nextInt(random.nextInt(120) + 8)
      val l22 = z + random.nextInt(16) + 8
      new WorldGenLiquids(Blocks.FLOWING_WATER).generate(world, random.self, new BlockPos(j20, l21, l22))
    }

    for (l17 <- 0 until 20) {
      val k20 = x + random.nextInt(16) + 8
      val i22 = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8)
      val i23 = z + random.nextInt(16) + 8
      new WorldGenLiquids(Blocks.FLOWING_LAVA).generate(world, random.self, new BlockPos(k20, i22, i23))
    }

    for (x2 <- 0 until 16;
         z2 <- 0 until 16) {
      val y1 = world.getTopSolidOrLiquidBlock(new BlockPos(x2 + x + 8, 0, z2 + z + 8)).getY
      val snowPos = new BlockPos(x2 + x + 8, y1, z2 + z + 8)
      if (world.canSnowAt(snowPos, true) && y1 > 0 && y1 < 128 &&
        world.isAirBlock(snowPos) &&
        world.getBlockState(snowPos.add(0, -1, 0)).getMaterial.isSolid &&
        world.getBlockState(snowPos.add(0, -1, 0)).getMaterial != Material.ICE) {
        world.setBlockState(snowPos, Blocks.SNOW_LAYER.getDefaultState)
      }
    }

    BlockFalling.fallInstantly = false
  }

}
