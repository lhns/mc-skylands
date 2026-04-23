package skylands.registry

import net.minecraft.world.level.block.Block
import skylands.block.{BeanBlock, BeanstalkBlock, CloudBlock}
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier

object SkylandsBlocks:
  val CLOUD: Supplier[Block] =
    SkylandsPlatform.current.registerBlock("cloud", () => SkylandsPlatform.current.newCloudBlock())
  val BEAN: Supplier[Block] =
    SkylandsPlatform.current.registerBlock("bean", () => new BeanBlock())
  val BEANSTALK: Supplier[Block] =
    SkylandsPlatform.current.registerBlock("beanstalk", () => new BeanstalkBlock())

  // Called once from SkylandsCommon.init(). The mere act of dispatching this
  // method initializes the enclosing object, which evaluates CLOUD/BEAN/BEANSTALK
  // and queues them with the platform.
  def register(): Unit = ()
