package skylands.registry

import net.minecraft.world.item.{BlockItem, Item}
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier

object SkylandsItems:
  val CLOUD: Supplier[Item] =
    SkylandsPlatform.current.registerItem(
      "cloud",
      () => new BlockItem(SkylandsBlocks.CLOUD.get(), new Item.Properties())
    )
  val BEAN: Supplier[Item] =
    SkylandsPlatform.current.registerItem(
      "bean",
      () => new BlockItem(SkylandsBlocks.BEAN.get(), new Item.Properties())
    )
  val BEANSTALK: Supplier[Item] =
    SkylandsPlatform.current.registerItem(
      "beanstalk",
      () => new BlockItem(SkylandsBlocks.BEANSTALK.get(), new Item.Properties())
    )

  def register(): Unit = ()
