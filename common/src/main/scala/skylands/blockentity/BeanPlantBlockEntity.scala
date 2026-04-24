package skylands.blockentity

import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
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
          val gen = beanstalkGenerator match
            case Some(g) => g
            case None =>
              if !fullyEncasedInDirt(sl, pos) then return
              val g = new BeanstalkGenerator(sl, pos)
              beanstalkGenerator = Some(g)
              g
          gen.update()
      case _ => ()

  // Gate generator creation on the bean being fully packed in dirt on all six
  // sides. Uses BlockTags.DIRT so coarse dirt / grass block / podzol / rooted
  // dirt / mycelium / mud / moss block count too. Only gates the initial
  // spawn — once the beanstalk has started growing we keep ticking even if a
  // neighbour gets mined out.
  private def fullyEncasedInDirt(level: ServerLevel, pos: BlockPos): Boolean =
    val dirs = Direction.values
    var i = 0
    while i < dirs.length do
      if !level.getBlockState(pos.relative(dirs(i))).is(BlockTags.DIRT) then return false
      i += 1
    true
