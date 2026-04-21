package skylands.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import skylands.registry.SkylandsBlockEntities

class BeanPlantBlockEntity(pos: BlockPos, state: BlockState)
    extends BlockEntity(SkylandsBlockEntities.BEAN_PLANT.get(), pos, state):

  def serverTick(level: Level, pos: BlockPos, state: BlockState): Unit =
    // Beanstalk growth lives in M4.
    ()
