package skylands.client

import dev.architectury.registry.client.rendering.RenderTypeRegistry
import net.minecraft.client.renderer.RenderType
import skylands.registry.SkylandsBlocks

object SkylandsClient:
  def init(): Unit =
    RenderTypeRegistry.register(RenderType.translucent(), SkylandsBlocks.CLOUD.get())
