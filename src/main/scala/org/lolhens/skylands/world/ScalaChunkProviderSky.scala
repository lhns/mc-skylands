package org.lolhens.skylands.world

import net.minecraft.world.World
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
  private val pnr = new Array[Double](256)
  private val ar = new Array[Double](256)
  private val br = new Array[Double](256)
  private val mapGenCaves = new MapGenCaves()


  private val lperlinNoise1 = new NoiseGeneratorOctaves(random.self, 16)
  private val lperlinNoise2 = new NoiseGeneratorOctaves(random.self, 16)
  private val perlinNoise1 = new NoiseGeneratorOctaves(random.self, 8)

  private def getNoiseArray(noiseArray: Array[Double], xOffset: Int, yOffset: Int, zOffset: Int, xSize: Int, ySize: Int, zSize: Int): Array[Double] = {
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
        val d13: Double = (z - (ySize - 32)).toFloat / (32 - 1.0F).toFloat
        noise = noise * (1.0D - d13) + -30D * d13
      }
      if (z < 8) {
        val d14: Double = (8 - z).toFloat / (8 - 1.0F).toFloat
        noise = noise * (1.0D - d14) + -30D * d14
      }
      noiseArray(index) = noise
      index += 1
    }

    noiseArray
  }
}
