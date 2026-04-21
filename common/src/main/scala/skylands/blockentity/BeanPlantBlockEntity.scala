package skylands.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import skylands.registry.SkylandsBlockEntities
import skylands.worldgen.BeanstalkGenerator

class BeanPlantBlockEntity(pos: BlockPos, state: BlockState)
    extends BlockEntity(SkylandsBlockEntities.BEAN_PLANT.get(), pos, state):

  private var beanstalkGenerator: Option[BeanstalkGenerator] = None

  def serverTick(level: Level, pos: BlockPos, state: BlockState): Unit =
    level match
      case sl: ServerLevel =>
        if sl.getRandom.nextInt(3) == 0 then
          val gen = beanstalkGenerator.getOrElse {
            val g = new BeanstalkGenerator(sl, pos)
            beanstalkGenerator = Some(g)
            g
          }
          gen.update()
      case _ => ()
