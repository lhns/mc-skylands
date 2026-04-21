package skylands.fabric

import net.fabricmc.api.ClientModInitializer
import skylands.client.SkylandsClient

// See SkylandsFabric — the Scala adapter needs a singleton object.
object SkylandsFabricClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    SkylandsClient.init()
