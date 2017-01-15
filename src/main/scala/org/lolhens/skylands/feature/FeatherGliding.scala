package org.lolhens.skylands.feature

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import org.lolhens.skylands.SkylandsMod

/**
  * Created by pierr on 15.01.2017.
  */
object FeatherGliding {
  def update(player: EntityPlayer): Unit = {
    val heldItems = List(player.getHeldItem(EnumHand.MAIN_HAND), player.getHeldItem(EnumHand.OFF_HAND)).flatMap(Option(_)).map(_.getItem)

    if (player.dimension == SkylandsMod.skylands.skylandsDimensionType.getId && heldItems.contains(Items.FEATHER) && !player.capabilities.isFlying) {
      if (player.motionY <= -0.3) {
        player.jumpMovementFactor = 0.1f
        player.motionY = Math.max(player.motionY, -0.3)
      }
      player.fallDistance = 0
    }
  }
}
