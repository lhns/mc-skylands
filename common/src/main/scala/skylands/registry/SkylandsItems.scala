package skylands.registry

import dev.architectury.registry.registries.{DeferredRegister, RegistrySupplier}
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.{BlockItem, Item}

import skylands.SkylandsCommon.ModId

object SkylandsItems:
  val ITEMS: DeferredRegister[Item] = DeferredRegister.create(ModId, Registries.ITEM)

  val CLOUD: RegistrySupplier[Item] = ITEMS.register(
    "cloud",
    () => new BlockItem(SkylandsBlocks.CLOUD.get(), new Item.Properties())
  )
  val BEAN: RegistrySupplier[Item] = ITEMS.register(
    "bean",
    () => new BlockItem(SkylandsBlocks.BEAN.get(), new Item.Properties())
  )
  val BEANSTALK: RegistrySupplier[Item] = ITEMS.register(
    "beanstalk",
    () => new BlockItem(SkylandsBlocks.BEANSTALK.get(), new Item.Properties())
  )

  def register(): Unit = ITEMS.register()
