package org.lolhens.skylands

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

  private var _skylands: Skylands = _

  def skylands: Skylands = _skylands

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    _skylands = proxy.skylands(event.getSuggestedConfigurationFile)
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    skylands.init()
  }
}
