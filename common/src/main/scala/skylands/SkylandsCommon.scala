package skylands

import org.slf4j.{Logger, LoggerFactory}
import skylands.gameplay.FeatherGliding
import skylands.loot.BeanLootInjection
import skylands.platform.SkylandsPlatform
import skylands.registry.{SkylandsBlockEntities, SkylandsBlocks, SkylandsCreativeTabs, SkylandsItems, SkylandsWorldgen}
import skylands.teleport.FallIntoOverworld

object SkylandsCommon:
  val ModId: String = "skylands"
  val Log: Logger = LoggerFactory.getLogger(ModId)

  def init(): Unit =
    SkylandsBlocks.register()
    SkylandsBlockEntities.register()
    SkylandsItems.register()
    SkylandsCreativeTabs.register()
    SkylandsWorldgen.register()
    BeanLootInjection.register()
    SkylandsPlatform.current.onPlayerPostTick { player =>
      FallIntoOverworld.onPlayerTick(player)
      FeatherGliding.onPlayerTick(player)
    }
    Log.info("Skylands init (common)")
