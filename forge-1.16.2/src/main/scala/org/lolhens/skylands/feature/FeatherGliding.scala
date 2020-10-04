package org.lolhens.skylands.feature

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.util.Hand
import org.lolhens.skylands.SkylandsMod

/**
  * Created by pierr on 15.01.2017.
  */
object FeatherGliding {
  def update(player: PlayerEntity): Unit = {
    if (player.dimension == SkylandsMod.skylands.skylandsDimensionType.getId) {
      val heldItems = List(player.getHeldItem(Hand.MAIN_HAND), player.getHeldItem(Hand.OFF_HAND)).flatMap(Option(_)).map(_.getItem)

      if (heldItems.contains(Items.FEATHER) && !player.capabilities.isFlying) {
        if (player.getMotion.y <= -0.3) {
          player.jumpMovementFactor = 0.1f
          player.motionY = Math.max(player.motionY, -0.3)
        }
        player.fallDistance = 0
      }
    }
  }
}
