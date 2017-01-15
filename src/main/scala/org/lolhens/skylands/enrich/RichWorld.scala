package org.lolhens.skylands.enrich

import net.minecraft.world.World

import scala.language.implicitConversions

/**
  * Created by pierr on 15.01.2017.
  */
class RichWorld(val world: World) extends AnyVal {
  def keepLoaded(): Unit = world.provider.getDimensionType.setLoadSpawn(true)
}

object RichWorld {
  implicit def fromWorld(world: World): RichWorld = new RichWorld(world)
}
