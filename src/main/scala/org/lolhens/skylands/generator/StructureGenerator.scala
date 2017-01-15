package org.lolhens.skylands.generator

import net.minecraft.util.math.BlockPos
import net.minecraft.world.{World, WorldServer}

/**
  * Created by pierr on 15.01.2017.
  */
abstract class StructureGenerator(val world: WorldServer, val position: BlockPos) {
  def update(): Unit
}
