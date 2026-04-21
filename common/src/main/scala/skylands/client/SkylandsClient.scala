package skylands.client

import skylands.platform.SkylandsPlatform
import skylands.registry.SkylandsBlocks

object SkylandsClient:
  def init(): Unit =
    SkylandsPlatform.current.setTranslucentRenderType(SkylandsBlocks.CLOUD)
