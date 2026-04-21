package skylands.fabric

import net.fabricmc.api.ModInitializer
import skylands.SkylandsCommon

class SkylandsFabric extends ModInitializer:
  override def onInitialize(): Unit =
    SkylandsCommon.Log.info("Skylands init (fabric entry)")
    SkylandsCommon.init()
