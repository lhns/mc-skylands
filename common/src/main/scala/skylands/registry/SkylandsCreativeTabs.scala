package skylands.registry

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.{DeferredRegister, RegistrySupplier}
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.{CreativeModeTab, ItemStack}
import skylands.SkylandsCommon.ModId

object SkylandsCreativeTabs:
  val TABS: DeferredRegister[CreativeModeTab] =
    DeferredRegister.create(ModId, Registries.CREATIVE_MODE_TAB)

  val SKYLANDS: RegistrySupplier[CreativeModeTab] = TABS.register(
    "skylands",
    () =>
      CreativeTabRegistry.create(builder =>
        builder
          .title(Component.translatable("itemGroup.skylands"))
          .icon(() => new ItemStack(SkylandsItems.BEAN.get()))
          .displayItems((_, output) =>
            output.accept(SkylandsItems.CLOUD.get())
            output.accept(SkylandsItems.BEAN.get())
            output.accept(SkylandsItems.BEANSTALK.get())
          )
      )
  )

  def register(): Unit = TABS.register()
