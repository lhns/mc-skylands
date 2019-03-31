package org.lolhens.skylands.ops

import net.minecraft.util.math.BlockPos

import scala.language.implicitConversions

/**
  * Created by pierr on 16.01.2017.
  */
class BlockPosOps(val self: BlockPos) extends AnyVal {
  def +(blockPos: BlockPos): BlockPos = self.add(blockPos.getX, blockPos.getY, blockPos.getZ)

  def -(blockPos: BlockPos): BlockPos = self.add(-blockPos.getX, -blockPos.getY, -blockPos.getZ)

  def inSphere(radius: Double): Boolean = self.getX * self.getX + self.getY * self.getY + self.getZ * self.getZ < radius * radius
}

object BlockPosOps {
  implicit def fromBlockPos(blockPos: BlockPos): BlockPosOps = new BlockPosOps(blockPos)
}
