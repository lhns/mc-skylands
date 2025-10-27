package org.aeritas.skylands.generator

import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.NoiseGeneratorOctaves
import org.aeritas.skylands.SkylandsMod
import org.aeritas.skylands.enrich.RichBlockPos._
import org.aeritas.skylands.world.BlockArray

class CloudGenerator(seed: Long) {
  private val random = new java.util.Random(seed)
  private val cloudNoise = new NoiseGeneratorOctaves(random, 4)
  
  // Cache commonly used values
  private val cloudState = SkylandsMod.instance.blockCloud.getDefaultState
  private val noiseCache = new Array[Double](16 * 16 * 8) // 8 vertical levels
  private val cloudShapes = Array.tabulate(6)(size => precomputeCloudShape(3 + size))
  
  def generate(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val baseX = chunkX * 16
    val baseZ = chunkZ * 16
    
    // Generate all noise values at once for better performance
    cloudNoise.generateNoiseOctaves(noiseCache, baseX, 64, baseZ, 16, 8, 16, 0.05, 0.05, 0.05)
    
    var idx = 0
    var x = 0
    while (x < 16) {
      var z = 0
      while (z < 16) {
        var y = 64
        while (y < 192) {
          val noiseIdx = ((x * 16 + z) * 8 + ((y - 64) >> 4))
          val noise = noiseCache(noiseIdx)
          
          if (noise > 0.3) {
            generateCloud(new BlockPos(baseX + x, y, baseZ + z), primer)
          }
          
          y += 16
        }
        z += 1
      }
      x += 1
    }
  }

  private def generateCloud(center: BlockPos, primer: ChunkPrimer): Unit = {
    val cloudSizeIndex = random.nextInt(6)
    val shape = cloudShapes(cloudSizeIndex)
    
    var i = 0
    while (i < shape.length) {
      val (xOff, yOff, zOff) = shape(i)
      val pos = center.add(xOff, yOff, zOff)
      if (pos.getY >= 0 && pos.getY < 256) {
        val localX = pos.getX & 15
        val localZ = pos.getZ & 15
        primer.setBlockState(localX, pos.getY, localZ, cloudState)
      }
      i += 1
    }
  }
  
  // Precompute cloud shapes for each possible size
  private def precomputeCloudShape(cloudSize: Int): Array[(Int, Int, Int)] = {
    val points = for {
      x <- -cloudSize to cloudSize
      y <- -1 to 1
      z <- -cloudSize to cloudSize
      if x * x + y * y * 4 + z * z <= cloudSize * cloudSize
    } yield (x, y, z)
    points.toArray
  }
}