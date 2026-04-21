package skylands.registry

import dev.architectury.registry.registries.{DeferredRegister, RegistrySupplier}
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import skylands.SkylandsCommon.ModId
import skylands.block.{BeanBlock, BeanstalkBlock, CloudBlock}

object SkylandsBlocks:
  val BLOCKS: DeferredRegister[Block] = DeferredRegister.create(ModId, Registries.BLOCK)

  val CLOUD: RegistrySupplier[Block] = BLOCKS.register("cloud", () => new CloudBlock())
  val BEAN: RegistrySupplier[Block] = BLOCKS.register("bean", () => new BeanBlock())
  val BEANSTALK: RegistrySupplier[Block] = BLOCKS.register("beanstalk", () => new BeanstalkBlock())

  def register(): Unit = BLOCKS.register()
