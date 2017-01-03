package org.lolhens.skylands

import java.io.File

import net.minecraft.item.ItemBlock
import net.minecraft.world.DimensionType
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lolhens.skylands.blocks.BlockPortal
import org.lolhens.skylands.world.WorldProviderSkylands

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val portal = new BlockPortal()
  GameRegistry.register(portal.setRegistryName("portal"))
  GameRegistry.register(new ItemBlock(portal).setRegistryName(portal.getRegistryName))

  val dimensionType: DimensionType = DimensionType.register("Skylands", "sky", config.dimensionId, classOf[WorldProviderSkylands], false)
  DimensionManager.registerDimension(config.dimensionId, dimensionType)

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  def onItemPickup(event: EntityItemPickupEvent): Unit = {
    println("ITEM PICKUP")
    /*if (event.getItem.getEntityItem.getItem == Items.GOLDEN_APPLE) {
      println("APPLE")
      event.getEntityPlayer match {
        case player: EntityPlayerMP =>
          player.getServer.getPlayerList.transferPlayerToDimension(player, dimensionType.getId, new SkylandsTeleporter(event.getEntityPlayer.getServer.worldServerForDimension(skylandsDimensionType.getId)))

      }
      //event.getEntityPlayer.changeDimension(skylandsDimensionType.getId)
    }*/
  }
}
