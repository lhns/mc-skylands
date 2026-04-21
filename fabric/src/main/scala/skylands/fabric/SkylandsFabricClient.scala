package skylands.fabric

import net.fabricmc.api.ClientModInitializer
import skylands.client.SkylandsClient

class SkylandsFabricClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    SkylandsClient.init()
