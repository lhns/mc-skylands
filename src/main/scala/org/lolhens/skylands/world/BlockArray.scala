package org.lolhens.skylands.world

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by pierr on 20.01.2017.
  */
trait BlockArray {
  protected def get[E](position: BlockPos, function: (IBlockAccess, BlockPos) => (E)): E

  def setBlockState(position: BlockPos, blockState: IBlockState): Unit

  def getBlockState(position: BlockPos): IBlockState = get(position, _.getBlockState(_))

  def isReplaceable(position: BlockPos): Boolean = {
    val blockState = getBlockState(position)
    blockState.getMaterial.isReplaceable ||
      get(position, (blockAccess, pos) => blockState.getBlock.isLeaves(blockState, blockAccess, pos)) ||
      Seq(Material.AIR, Material.LEAVES).contains(blockState.getMaterial) ||
      Seq(Blocks.SAPLING, Blocks.VINE).contains(blockState.getBlock)
  }

  def isTerrainBlock(position: BlockPos): Boolean =
    Seq(Blocks.GRASS, Blocks.DIRT, Blocks.STONE).contains(getBlockState(position).getBlock)
}

object BlockArray {
  def apply(world: World) = new BlockArray {
    override protected def get[E](position: BlockPos, function: (IBlockAccess, BlockPos) => E): E = function(world, position)

    override def setBlockState(position: BlockPos, blockState: IBlockState): Unit = world.setBlockState(position, blockState)
  }

  def syncVertical(bottom: World, top: World, overlap: Int) = new BlockArray {
    override protected def get[E](position: BlockPos, function: (IBlockAccess, BlockPos) => E): E =
      if (position.getY <= 255)
        function(bottom, position)
      else
        function(top, position.add(0, overlap - 255, 0))

    override def setBlockState(position: BlockPos, blockState: IBlockState): Unit = {
      if (position.getY <= 255) bottom.setBlockState(position, blockState)
      if (position.getY >= 255 - overlap) top.setBlockState(position.add(0, overlap - 255, 0), blockState)
    }
  }
}
