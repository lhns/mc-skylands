package de.lolhens.minecraft.skylandsmod.util

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait BlockStateAccess {
  def getBlockState(pos: BlockPos): BlockState

  def setBlockState(pos: BlockPos, state: BlockState, flags: Int): Unit

  def setBlockState(pos: BlockPos, state: BlockState): Unit = setBlockState(pos, state, 3)
}

object BlockStateAccess {
  def world(world: World): BlockStateAccess = new BlockStateAccess {
    override def getBlockState(pos: BlockPos): BlockState = world.getBlockState(pos)

    override def setBlockState(pos: BlockPos, state: BlockState, flags: Int): Unit = world.setBlockState(pos, state, flags)
  }

  def verticallyStackedWithOverlap(lower: BlockStateAccess,
                                   upper: BlockStateAccess,
                                   overlap: Int): BlockStateAccess = new BlockStateAccess {
    override def getBlockState(pos: BlockPos): BlockState =
      if (pos.getY <= 255)
        lower.getBlockState(pos)
      else
        upper.getBlockState(pos.add(0, overlap - 255, 0))

    override def setBlockState(pos: BlockPos, state: BlockState, flags: Int): Unit = {
      if (pos.getY <= 255)
        lower.setBlockState(pos, state)

      if (pos.getY >= 255 - overlap)
        upper.setBlockState(pos.add(0, overlap - 255, 0), state)
    }
  }
}
