package skylands.registry

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import skylands.blockentity.BeanPlantBlockEntity
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier

object SkylandsBlockEntities:
  val BEAN_PLANT: Supplier[BlockEntityType[BeanPlantBlockEntity]] =
    SkylandsPlatform.current.registerBlockEntityType[BeanPlantBlockEntity](
      "bean_plant",
      () =>
        BlockEntityType.Builder
          .of(
            (pos: BlockPos, state: BlockState) => new BeanPlantBlockEntity(pos, state),
            SkylandsBlocks.BEAN.get()
          )
          .build(null)
    )

  def register(): Unit = ()
