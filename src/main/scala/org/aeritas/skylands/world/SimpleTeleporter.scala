package org.aeritas.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer

abstract class AbstractTeleporter(world: WorldServer) extends net.minecraft.world.Teleporter(world) {
  override def placeInPortal(entity: Entity, rotationYaw: Float): Unit = {
    val pos = findSafePosition(entity)
    entity.setLocationAndAngles(pos.getX + 0.5, pos.getY, pos.getZ + 0.5, entity.rotationYaw, 0.0f)
    entity.motionX = 0.0
    entity.motionY = 0.0
    entity.motionZ = 0.0
  }

  protected def findSafePosition(entity: Entity): BlockPos
}

class SimpleTeleporter(world: WorldServer) extends AbstractTeleporter(world) {
  protected def findSafePosition(entity: Entity): BlockPos = {
    val pos = new BlockPos(entity)
    var safePos = pos
    
    // Look for a safe position within 16 blocks vertically
    for (y <- 0 to 32) {
      if (isSafePosition(pos.up(y))) {
        safePos = pos.up(y)
      } else if (isSafePosition(pos.down(y))) {
        safePos = pos.down(y)
      }
    }
    
    safePos
  }
  
  private def isSafePosition(pos: BlockPos): Boolean = {
    world.isAirBlock(pos) && world.isAirBlock(pos.up()) && !world.isAirBlock(pos.down())
  }
}