package skylands.fabric

import net.fabricmc.api.ModInitializer
import skylands.SkylandsCommon

// Scala object (not class) so Krysztal's ScalaLanguageAdapter finds MODULE$
// via reflection on SkylandsFabric$.
object SkylandsFabric extends ModInitializer:
  override def onInitialize(): Unit =
    SkylandsCommon.Log.info("Skylands init (fabric entry)")
    SkylandsCommon.init()
