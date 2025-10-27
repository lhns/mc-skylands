package org.aeritas.skylands.world

import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.ChunkPrimer

class BlockArray(size: Int) {
  private val blocks = new Array[(BlockPos, IBlockState)](size)
  private var index = 0

  def setBlock(pos: BlockPos, state: IBlockState): Unit = {
    if (index < blocks.length) {
      blocks(index) = (pos, state)
      index += 1
    }
  }

  def place(primer: ChunkPrimer): Unit = {
    val chunkX = 0
    val chunkZ = 0
    
    for (i <- 0 until index) {
      val (pos, state) = blocks(i)
      val localX = pos.getX & 15
      val localZ = pos.getZ & 15
      val y = pos.getY
      
      if (y >= 0 && y < 256) {
        primer.setBlockState(localX, y, localZ, state)
      }
    }
  }
}