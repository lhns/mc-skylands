package org.aeritas.skylands

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Loader, Mod, SidedProxy}
import org.aeritas.skylands.integration.bop.BiomesOPlentyIntegration
import org.aeritas.skylands.proxy.CommonProxy

@Mod(modid = SkylandsMod.ModId, version = SkylandsMod.Version, modLanguage = "scala")
object SkylandsMod {
  final val ModId = "skylandsmod"
  final val Version = "0.2.0"

  @SidedProxy(clientSide = "org.aeritas.skylands.proxy.ClientProxy", serverSide = "org.aeritas.skylands.proxy.CommonProxy")
  var proxy: CommonProxy = _

  var instance: SkylandsMod = _

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    instance = new SkylandsMod(event.getSuggestedConfigurationFile)
    if (Loader.isModLoaded("biomesoplenty")) {
      BiomesOPlentyIntegration.initialize()
    }
    proxy.preInit()
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.init()
    MinecraftForge.EVENT_BUS.register(instance)
  }
}