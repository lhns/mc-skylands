package org.aeritas.skylands.enrich

import net.minecraft.world.chunk.Chunk
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

object RichChunk {
  implicit class RichChunk(val chunk: Chunk) extends AnyVal {
    def getBlockState(pos: BlockPos): IBlockState = {
      chunk.getBlockState(pos.getX & 15, pos.getY, pos.getZ & 15)
    }

    def setBlockState(pos: BlockPos, state: IBlockState): Unit = {
      chunk.setBlockState(pos, state)
    }

    def isLoaded: Boolean = {
      chunk.isLoaded
    }

    def needsSaving: Boolean = {
      chunk.needsSaving(false)
    }

    def markDirty(): Unit = {
      chunk.markDirty()
    }
  }
}