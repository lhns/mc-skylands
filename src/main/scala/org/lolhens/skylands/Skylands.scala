package org.lolhens.skylands

import java.io.File

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemBlock
import net.minecraft.world.DimensionType
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lolhens.skylands.block.{BlockBean, BlockBeanStem, BlockCloud, BlockPortal}
import org.lolhens.skylands.feature.{FallIntoOverworld, FeatherGliding}
import org.lolhens.skylands.tileentities.TileEntityBeanPlant
import org.lolhens.skylands.world.WorldProviderSkylands

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val blockPortal = new BlockPortal()
  GameRegistry.register(blockPortal.setRegistryName("portal"))
  GameRegistry.register(new ItemBlock(blockPortal).setRegistryName(blockPortal.getRegistryName))

  val blockBeanStem = new BlockBeanStem()
  GameRegistry.register(blockBeanStem.setRegistryName("beanstem"))
  GameRegistry.register(new ItemBlock(blockBeanStem).setRegistryName(blockBeanStem.getRegistryName))

  val blockBean = new BlockBean()
  GameRegistry.register(blockBean.setRegistryName("bean"))
  GameRegistry.register(new ItemBlock(blockBean).setRegistryName(blockBean.getRegistryName))
  GameRegistry.registerTileEntity(classOf[TileEntityBeanPlant], "bean_tile_entity")

  val blockCloud = new BlockCloud()
  GameRegistry.register(blockCloud.setRegistryName("cloud"))
  GameRegistry.register(new ItemBlock(blockCloud).setRegistryName(blockCloud.getRegistryName))

  val skylandsDimensionType: DimensionType = DimensionType.register("Skylands", "sky", config.dimensionId, classOf[WorldProviderSkylands], false)
  DimensionManager.registerDimension(config.dimensionId, skylandsDimensionType)

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  def onWorldLoad(event: WorldEvent.Unload): Unit = {
    println(event.getWorld)
    println(event.getPhase)
  }

  def keepSkylandsLoaded(): Unit = {
    skylandsDimensionType.setLoadSpawn(true)
    _keepSkylandsLoaded = true
  }

  private var _keepSkylandsLoaded = false

  @SubscribeEvent
  def onWorldTick(event: TickEvent.WorldTickEvent): Unit = {
    if (event.world.provider.getDimensionType == skylandsDimensionType) {
      if (_keepSkylandsLoaded)
        _keepSkylandsLoaded = false
      else if (skylandsDimensionType.shouldLoadSpawn())
        skylandsDimensionType.setLoadSpawn(false)
    }
  }

  @SubscribeEvent
  def onPlayerTick(event: TickEvent.PlayerTickEvent): Unit = {
    val player = event.player

    player match {
      case player: EntityPlayerMP =>
        FallIntoOverworld.update(player)

      case _ =>
    }

    FeatherGliding.update(player)
  }
}
