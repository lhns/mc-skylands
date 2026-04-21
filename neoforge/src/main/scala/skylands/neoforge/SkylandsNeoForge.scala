package skylands.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import skylands.SkylandsCommon

@Mod("skylands")
class SkylandsNeoForge(modBus: IEventBus):
  SkylandsCommon.Log.info("Skylands init (neoforge entry)")
  SkylandsCommon.init()
