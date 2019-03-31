package org.lolhens.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.math.Vec3d
import net.minecraft.world.WorldServer
import org.lolhens.skylands.ops.EntityOps._

/**
  * Created by pierr on 14.01.2017.
  */
class SimpleTeleporter(world: WorldServer,
                       position: Vec3d,
                       motion: Vec3d,
                       yaw: Float, pitch: Float) extends AbstractTeleporter(world) {
  override def teleport(entity: Entity, rotationYaw: Float): Unit = {
    entity.setPositionVectorAndRotation(position, yaw, pitch)
    entity.setMotionVector(motion)

    entity match {
      case player: EntityPlayerMP =>
        player.connection.update()

      case _ =>
    }
  }
}
