package skylands.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import skylands.SkylandsCommon
import skylands.client.SkylandsClient

@Mod("skylands")
class SkylandsNeoForge(modBus: IEventBus):
  SkylandsCommon.Log.info("Skylands init (neoforge entry)")
  SkylandsCommon.init()

  if FMLEnvironment.dist == Dist.CLIENT then
    modBus.addListener((_: FMLClientSetupEvent) => SkylandsClient.init())
