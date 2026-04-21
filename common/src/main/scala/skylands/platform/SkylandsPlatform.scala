package skylands.platform

import com.mojang.serialization.MapCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{CreativeModeTab, Item}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.storage.loot.{LootPool, LootTable}

import java.util.function.Supplier

// Cross-loader SPI. Replaces the handful of dev.architectury.* entry points
// the port relied on (DeferredRegister, TickEvent, LootEvent, CreativeTabRegistry,
// RenderTypeRegistry). Each platform's entrypoint installs its impl before
// SkylandsCommon.init() runs.
trait SkylandsPlatform:
  def registerBlock[B <: Block](id: String, factory: () => B): Supplier[B]

  def registerItem[I <: Item](id: String, factory: () => I): Supplier[I]

  def registerBlockEntityType[T <: BlockEntity](
      id: String,
      factory: () => BlockEntityType[T]
  ): Supplier[BlockEntityType[T]]

  def registerCreativeTab(id: String, factory: () => CreativeModeTab): Supplier[CreativeModeTab]

  def registerChunkGenerator(id: String, codec: MapCodec[? <: ChunkGenerator]): Unit

  def onPlayerPostTick(callback: Player => Unit): Unit

  def onLootTableModify(
      targets: java.util.Set[ResourceKey[LootTable]],
      poolFactory: () => LootPool.Builder
  ): Unit

  def setTranslucentRenderType(block: Supplier[? <: Block]): Unit

object SkylandsPlatform:
  @volatile private var impl: SkylandsPlatform = null

  def install(platform: SkylandsPlatform): Unit = impl = platform

  def current: SkylandsPlatform =
    val p = impl
    if p == null then
      throw new IllegalStateException(
        "SkylandsPlatform not installed — each loader's entrypoint must call SkylandsPlatform.install(...) before SkylandsCommon.init()"
      )
    p
