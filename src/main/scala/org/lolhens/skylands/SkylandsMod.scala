package org.lolhens.skylands

import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent

/**
  * Created by pierr on 31.12.2016.
  */
@Mod(modid = SkylandsMod.ModId, version = SkylandsMod.Version)
class SkylandsMod {
  @EventHandler
  def init(event: FMLInitializationEvent) {
    println("DIRT BLOCK >> " + Blocks.DIRT.getUnlocalizedName)
  }
}

object SkylandsMod {
  final val ModId = "skylandsmod"

  final val Version = "0.0.0"
}
