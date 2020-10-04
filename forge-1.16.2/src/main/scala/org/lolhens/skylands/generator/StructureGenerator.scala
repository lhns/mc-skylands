package org.lolhens.skylands.generator

import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import net.minecraft.world.server.ServerWorld

/**
  * Created by pierr on 15.01.2017.
  */
abstract class StructureGenerator(val world: ServerWorld, val position: BlockPos) {
  def update(): Unit
}
