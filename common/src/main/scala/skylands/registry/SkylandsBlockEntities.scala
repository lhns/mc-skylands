package skylands.registry

import dev.architectury.registry.registries.{DeferredRegister, RegistrySupplier}
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import skylands.SkylandsCommon.ModId
import skylands.blockentity.BeanPlantBlockEntity

object SkylandsBlockEntities:
  val BLOCK_ENTITY_TYPES: DeferredRegister[BlockEntityType[?]] =
    DeferredRegister.create(ModId, Registries.BLOCK_ENTITY_TYPE)

  val BEAN_PLANT: RegistrySupplier[BlockEntityType[BeanPlantBlockEntity]] =
    BLOCK_ENTITY_TYPES
      .register(
        "bean_plant",
        () =>
          BlockEntityType.Builder
            .of(
              (pos: BlockPos, state: BlockState) => new BeanPlantBlockEntity(pos, state),
              SkylandsBlocks.BEAN.get()
            )
            .build(null)
      )
      .asInstanceOf[RegistrySupplier[BlockEntityType[BeanPlantBlockEntity]]]

  def register(): Unit = BLOCK_ENTITY_TYPES.register()
