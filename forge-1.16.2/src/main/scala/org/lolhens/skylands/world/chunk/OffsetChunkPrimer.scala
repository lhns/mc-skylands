package org.lolhens.skylands.world.chunk

import net.minecraft.block.BlockState
import net.minecraft.block.state.IBlockState
import net.minecraft.world.chunk.ChunkPrimer

/**
  * Created by pierr on 20.01.2017.
  */
class OffsetChunkPrimer(primer: ChunkPrimer, offset: Int) extends ChunkPrimer {
  override def setBlockState(x: Int, y: Int, z: Int, state: BlockState): Unit = primer.setBlockState(x, y + offset, z, state)

  override def getBlockState(x: Int, y: Int, z: Int): BlockState = primer.getBlockState(x, y + offset, z)

  override def findGroundBlockIdx(x: Int, z: Int): Int = primer.findGroundBlockIdx(x, z) + offset
}
