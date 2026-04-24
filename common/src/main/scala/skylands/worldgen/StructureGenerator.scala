package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

// 1.12.2 org.lolhens.skylands.generator.StructureGenerator
abstract class StructureGenerator(val level: ServerLevel, val position: BlockPos):
  def update(): Unit
