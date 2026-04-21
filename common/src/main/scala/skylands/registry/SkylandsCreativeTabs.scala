package skylands.registry

import net.minecraft.network.chat.Component
import net.minecraft.world.item.{CreativeModeTab, ItemStack}
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier

object SkylandsCreativeTabs:
  val SKYLANDS: Supplier[CreativeModeTab] =
    SkylandsPlatform.current.registerCreativeTab(
      "skylands",
      () =>
        // 2-arg builder(row, column) instead of no-arg — Fabric's remapped
        // CreativeModeTab class drops the no-arg overload. Row.TOP, col=0 is
        // the usual "just put it somewhere reasonable in the creative menu" slot.
        CreativeModeTab
          .builder(CreativeModeTab.Row.TOP, 0)
          .title(Component.translatable("itemGroup.skylands"))
          .icon(() => new ItemStack(SkylandsItems.BEAN.get()))
          .displayItems((_, output) =>
            output.accept(SkylandsItems.CLOUD.get())
            output.accept(SkylandsItems.BEAN.get())
            output.accept(SkylandsItems.BEANSTALK.get())
          )
          .build()
    )

  def register(): Unit = ()
