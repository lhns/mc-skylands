package org.aeritas.skylands.feature

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{PlayerTickEvent, Phase}
import org.aeritas.skylands.SkylandsMod
import org.aeritas.skylands.world.SimpleTeleporter

class FallIntoOverworld {
  @SubscribeEvent
  def onPlayerTick(event: PlayerTickEvent): Unit = {
    if (event.phase == Phase.START) {
      val player = event.player
      if (player.dimension == SkylandsMod.instance.skylandsDimensionType.getId && !player.capabilities.isCreativeMode && player.posY < 0) {
        if (!player.world.isRemote && player.isInstanceOf[EntityPlayerMP]) {
          val serverPlayer = player.asInstanceOf[EntityPlayerMP]
          serverPlayer.server.getPlayerList.transferPlayerToDimension(serverPlayer, 0, new SimpleTeleporter(serverPlayer.server.getWorld(0)))
        }
      }
    }
  }
}