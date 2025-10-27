package org.aeritas.skylands.world.chunk

import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.NoiseGeneratorOctaves

class SkylandsTerrainGenerator(world: World) {
  private val random = new java.util.Random(world.getSeed)
  private val noiseGen = new NoiseGeneratorOctaves(random, 4)
  private val heightNoise = new NoiseGeneratorOctaves(random, 2)
  
  // Cache noise arrays to reduce allocation
  private val noiseArray = new Array[Double](256)
  private val heightNoiseArray = new Array[Double](256)
  
  // Cache commonly used block states
  private val stoneState = Blocks.STONE.getDefaultState
  private val airState = Blocks.AIR.getDefaultState
  
  // Cache last generated chunk's density for smoother transitions
  private var lastChunkX = Int.MinValue
  private var lastChunkZ = Int.MinValue
  private val densityCache = new Array[Double](16 * 16 * 256)
  
  def generate(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = {
    val density = if (chunkX == lastChunkX + 1 || chunkX == lastChunkX - 1 || 
                     chunkZ == lastChunkZ + 1 || chunkZ == lastChunkZ - 1) {
      // Reuse cached density for neighboring chunks
      System.arraycopy(densityCache, 0, densityCache, 0, densityCache.length)
      updateDensityMap(chunkX, chunkZ, densityCache)
    } else {
      generateDensityMap(chunkX, chunkZ)
    }
    
    lastChunkX = chunkX
    lastChunkZ = chunkZ
    System.arraycopy(density, 0, densityCache, 0, density.length)
    
    generateTerrain(density, primer)
  }

  private def generateDensityMap(chunkX: Int, chunkZ: Int): Array[Double] = {
    val density = new Array[Double](16 * 16 * 256)
    updateDensityMap(chunkX, chunkZ, density)
  }
  
  private def updateDensityMap(chunkX: Int, chunkZ: Int, density: Array[Double]): Array[Double] = {
    val offsetX = chunkX * 16
    val offsetZ = chunkZ * 16
    
    // Generate base noise for the chunk
    noiseGen.generateNoiseOctaves(noiseArray, offsetX, 0, offsetZ, 16, 256, 16, 0.1, 0.1, 0.1)
    heightNoise.generateNoiseOctaves(heightNoiseArray, offsetX, offsetZ, 16, 16, 0.01, 0.01, false)
    
    // Process noise in optimized loops - separate X-Z plane from Y axis
    var idx = 0
    var xzIdx = 0
    while (xzIdx < 256) { // 16 * 16
      val x = xzIdx >> 4
      val z = xzIdx & 15
      val heightScale = heightNoiseArray(xzIdx) * 15 + 50
      
      var y = 0
      while (y < 256) {
        val noise = noiseArray(idx)
        val heightValue = math.abs(y - heightScale) * 0.1
        density(idx) = noise - heightValue
        
        idx += 1
        y += 1
      }
      xzIdx += 1
    }
    
    density
  }

  private def generateTerrain(density: Array[Double], primer: ChunkPrimer): Unit = {
    // Use a single loop to process all blocks, with optimized index calculation
    var index = 0
    while (index < 65536) { // 16 * 16 * 256
      val x = (index >> 12) & 15
      val z = (index >> 8) & 15
      val y = index & 255
      
      val state = if (density(index) > 0) stoneState else airState
      primer.setBlockState(x, y, z, state)
      
      index += 1
    }
  }
  
  // Helper method to calculate 3D array index
  @inline private def getIndex(x: Int, y: Int, z: Int): Int = {
    y + (z * 256) + (x * 256 * 16)
  }
}