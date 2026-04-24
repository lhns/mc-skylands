package skylands.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.level.block.{Block, EntityBlock, SoundType}
import net.minecraft.world.level.material.MapColor
import skylands.blockentity.BeanPlantBlockEntity
import skylands.registry.SkylandsBlockEntities

class BeanBlock extends Block(BeanBlock.Properties), EntityBlock:
  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
    new BeanPlantBlockEntity(pos, state)

  override def getTicker[T <: BlockEntity](
      level: Level,
      state: BlockState,
      beType: BlockEntityType[T]
  ): BlockEntityTicker[T] | Null =
    if beType == SkylandsBlockEntities.BEAN_PLANT.get() then
      val ticker: BlockEntityTicker[BeanPlantBlockEntity] =
        (lvl: Level, pos: BlockPos, st: BlockState, be: BeanPlantBlockEntity) => be.serverTick(lvl, pos, st)
      ticker.asInstanceOf[BlockEntityTicker[T]]
    else null

object BeanBlock:
  private val Properties: BlockBehaviour.Properties =
    BlockBehaviour.Properties.of()
      .mapColor(MapColor.PLANT)
      .strength(0.8f, 3f)
      .sound(SoundType.WOOD)
