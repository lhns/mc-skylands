package skylands.worldgen.noise

import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.synth.ImprovedNoise

// Faithful port of 1.12.2 net.minecraft.world.gen.NoiseGeneratorPerlin (2D).
// Used for the tree-density noise in the Skylands populate pass.
class PerlinNoise2D(random: RandomSource, levels: Int):
  private val noiseLevels: Array[ImprovedNoise] =
    Array.fill(levels)(new ImprovedNoise(random))

  def getValue(x: Double, z: Double): Double =
    var result = 0.0
    var scale = 1.0
    var i = 0
    while i < levels do
      result += noiseLevels(i).noise(x * scale, 0.0, z * scale) / scale
      scale /= 2.0
      i += 1
    result
