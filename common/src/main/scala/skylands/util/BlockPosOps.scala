package skylands.util

import net.minecraft.core.BlockPos

// Small 1.12.2-era BlockPos helpers ported as Scala 3 extensions.
extension (self: BlockPos)
  def +(other: BlockPos): BlockPos = self.offset(other.getX, other.getY, other.getZ)
  def -(other: BlockPos): BlockPos = self.offset(-other.getX, -other.getY, -other.getZ)
  def inSphere(radius: Double): Boolean =
    val x = self.getX.toDouble
    val y = self.getY.toDouble
    val z = self.getZ.toDouble
    x * x + y * y + z * z < radius * radius
