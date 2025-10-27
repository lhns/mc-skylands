package org.aeritas.skylands.proxy

import net.minecraftforge.common.MinecraftForge
import org.aeritas.skylands.feature.{FallIntoOverworld, FeatherGliding}

class ClientProxy extends CommonProxy {
  override def preInit(): Unit = {
    super.preInit()
  }

  override def init(): Unit = {
    super.init()
    MinecraftForge.EVENT_BUS.register(new FeatherGliding)
    MinecraftForge.EVENT_BUS.register(new FallIntoOverworld)
  }
}