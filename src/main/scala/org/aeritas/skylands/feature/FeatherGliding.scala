package org.aeritas.skylands.feature

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class FeatherGliding {
  @SubscribeEvent
  def onLivingUpdate(event: LivingUpdateEvent): Unit = {
    val entity = event.getEntityLiving
    if (!entity.world.isRemote && entity.isInstanceOf[EntityPlayer]) {
      val player = entity.asInstanceOf[EntityPlayer]
      if (!player.capabilities.isFlying && !player.onGround && player.motionY < 0.0D) {
        player.motionY *= 0.6D
      }
    }
  }
}