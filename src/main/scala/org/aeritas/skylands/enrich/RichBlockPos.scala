package org.aeritas.skylands.enrich

import net.minecraft.util.math.BlockPos

object RichBlockPos {
  implicit class RichBlockPos(val blockPos: BlockPos) extends AnyVal {
    def getX: Int = blockPos.getX
    def getY: Int = blockPos.getY
    def getZ: Int = blockPos.getZ
    
    def +(that: BlockPos): BlockPos = blockPos.add(that.getX, that.getY, that.getZ)
    def -(that: BlockPos): BlockPos = blockPos.add(-that.getX, -that.getY, -that.getZ)
    
    def +(x: Int, y: Int, z: Int): BlockPos = blockPos.add(x, y, z)
    def -(x: Int, y: Int, z: Int): BlockPos = blockPos.add(-x, -y, -z)
    
    def up(n: Int = 1): BlockPos = blockPos.up(n)
    def down(n: Int = 1): BlockPos = blockPos.down(n)
    def north(n: Int = 1): BlockPos = blockPos.north(n)
    def south(n: Int = 1): BlockPos = blockPos.south(n)
    def east(n: Int = 1): BlockPos = blockPos.east(n)
    def west(n: Int = 1): BlockPos = blockPos.west(n)
  }
}