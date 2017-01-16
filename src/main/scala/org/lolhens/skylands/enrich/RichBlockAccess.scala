package org.lolhens.skylands.enrich

import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

import scala.language.implicitConversions

/**
  * Created by pierr on 15.01.2017.
  */
class RichBlockAccess(val self: IBlockAccess) extends AnyVal {
  def isReplaceable(position: BlockPos): Boolean = {
    val blockState = self.getBlockState(position)
    blockState.getMaterial.isReplaceable ||
      blockState.getBlock.isLeaves(blockState, self, position) ||
      Seq(Material.AIR, Material.LEAVES).contains(blockState.getMaterial) ||
      Seq(Blocks.SAPLING, Blocks.VINE).contains(blockState.getBlock)
  }

  def isTerrainBlock(position: BlockPos): Boolean =
    Seq(Blocks.GRASS, Blocks.DIRT, Blocks.STONE).contains(self.getBlockState(position).getBlock)
}

object RichBlockAccess {
  implicit def fromBlockAccess(blockAccess: IBlockAccess): RichBlockAccess = new RichBlockAccess(blockAccess)
}
