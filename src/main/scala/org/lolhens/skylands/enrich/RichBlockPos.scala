package org.lolhens.skylands.enrich

import net.minecraft.util.math.BlockPos

/**
  * Created by pierr on 16.01.2017.
  */
class RichBlockPos(val self: BlockPos) extends AnyVal {
  def +(blockPos: BlockPos): BlockPos = self.add(blockPos.getX, blockPos.getY, blockPos.getZ)

  def -(blockPos: BlockPos): BlockPos = self.add(-blockPos.getX, -blockPos.getY, -blockPos.getZ)
}

object RichBlockPos {
  implicit def fromBlockPos(blockPos: BlockPos): RichBlockPos = new RichBlockPos(blockPos)
}
