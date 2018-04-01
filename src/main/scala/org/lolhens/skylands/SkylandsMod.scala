package org.lolhens.skylands

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import org.lolhens.skylands.proxy.CommonProxy

/**
  * Created by pierr on 31.12.2016.
  */
@Mod(modid = SkylandsMod.ModId, version = SkylandsMod.Version, modLanguage = "scala")
object SkylandsMod {
  final val ModId = "skylandsmod"

  final val Version = "0.1.1"


  @SidedProxy(clientSide = "org.lolhens.skylands.proxy.ClientProxy", serverSide = "org.lolhens.skylands.proxy.CommonProxy")
  var proxy: CommonProxy = _

  private[this] var _skylands: Skylands = _

  private def skylands_=(skylands: Skylands): Unit = _skylands = skylands

  def skylands: Skylands = _skylands

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    skylands = proxy.skylands(event.getSuggestedConfigurationFile)
    MinecraftForge.EVENT_BUS.register(skylands)
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    skylands.init()
  }
}
