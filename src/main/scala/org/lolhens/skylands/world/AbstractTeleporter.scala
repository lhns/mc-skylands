package org.lolhens.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.world.{Teleporter, WorldServer}

/**
  * Created by pierr on 14.01.2017.
  */
abstract class AbstractTeleporter(world: WorldServer) extends Teleporter(world) {
  override def placeInPortal(entityIn: Entity, rotationYaw: Float): Unit = teleport(entityIn, rotationYaw)

  def teleport(entityIn: Entity, rotationYaw: Float): Unit
}
