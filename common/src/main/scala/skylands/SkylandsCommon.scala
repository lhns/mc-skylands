package skylands

import org.slf4j.{Logger, LoggerFactory}
import skylands.registry.{SkylandsBlockEntities, SkylandsBlocks, SkylandsCreativeTabs, SkylandsItems}

object SkylandsCommon:
  val ModId: String = "skylands"
  val Log: Logger = LoggerFactory.getLogger(ModId)

  def init(): Unit =
    SkylandsBlocks.register()
    SkylandsBlockEntities.register()
    SkylandsItems.register()
    SkylandsCreativeTabs.register()
    Log.info("Skylands init (common)")
