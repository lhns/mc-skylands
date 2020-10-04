package org.lolhens.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.server.ServerWorld

/**
 * Created by pierr on 14.01.2017.
 */
class SimpleTeleporter(world: ServerWorld, targetPosition: Option[BlockPos] = None) extends AbstractTeleporter(world) {
  override def teleport(entity: Entity, rotationYaw: Float): Unit = {
    for (targetPosition <- targetPosition) {
      entity.setLocationAndAngles(targetPosition.getX + 0.5, targetPosition.getY + 1, targetPosition.getZ + 0.5, entity.rotationYaw, 0)
      entity.setMotion(0, 0, 0)

      entity match {
        case player: ServerPlayerEntity =>
          player.connection.update()
      }
    }
  }
}
