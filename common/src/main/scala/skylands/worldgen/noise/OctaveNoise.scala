package skylands.worldgen.noise

import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.synth.ImprovedNoise

import java.util.Arrays

// Faithful port of 1.12.2 net.minecraft.world.gen.NoiseGeneratorOctaves on top of
// 1.21's ImprovedNoise. Seeded with LegacyRandomSource wrapping the same Java Random
// the 1.12.2 code used, so per-octave permutation tables and xo/yo/zo offsets match.
class OctaveNoise(random: RandomSource, val octaves: Int):
  private val noiseLevels: Array[ImprovedNoise] =
    Array.fill(octaves)(new ImprovedNoise(random))

  def generateNoiseOctaves(
      target: Array[Double] | Null,
      xOffset: Int,
      yOffset: Int,
      zOffset: Int,
      xSize: Int,
      ySize: Int,
      zSize: Int,
      xScale: Double,
      yScale: Double,
      zScale: Double
  ): Array[Double] =
    val arr =
      if target == null then new Array[Double](xSize * ySize * zSize)
      else
        Arrays.fill(target, 0.0)
        target

    var scale = 1.0
    var j = 0
    while j < octaves do
      var x2 = xOffset.toDouble * scale * xScale
      val y2 = yOffset.toDouble * scale * yScale
      var z2 = zOffset.toDouble * scale * zScale
      val xWhole = Math.floor(x2).toLong
      val zWhole = Math.floor(z2).toLong
      x2 -= xWhole.toDouble
      z2 -= zWhole.toDouble
      x2 += (xWhole % 16777216L).toDouble
      z2 += (zWhole % 16777216L).toDouble

      addOctave(
        noiseLevels(j),
        arr,
        x2, y2, z2,
        xSize, ySize, zSize,
        xScale * scale, yScale * scale, zScale * scale,
        scale
      )

      scale /= 2.0
      j += 1
    arr

  private def addOctave(
      noise: ImprovedNoise,
      arr: Array[Double],
      xOff: Double, yOff: Double, zOff: Double,
      xSize: Int, ySize: Int, zSize: Int,
      xScale: Double, yScale: Double, zScale: Double,
      amplitude: Double
  ): Unit =
    val invAmp = 1.0 / amplitude
    var idx = 0
    var jx = 0
    while jx < xSize do
      val sx = xOff + jx.toDouble * xScale
      var jz = 0
      while jz < zSize do
        val sz = zOff + jz.toDouble * zScale
        var jy = 0
        while jy < ySize do
          val sy = yOff + jy.toDouble * yScale
          arr(idx) += noise.noise(sx, sy, sz) * invAmp
          idx += 1
          jy += 1
        jz += 1
      jx += 1
