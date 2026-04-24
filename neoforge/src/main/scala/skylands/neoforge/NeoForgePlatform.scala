package skylands.neoforge

import com.mojang.serialization.MapCodec
import net.minecraft.client.renderer.{ItemBlockRenderTypes, RenderType}
import net.minecraft.core.registries.Registries
import net.minecraft.resources.{ResourceKey, ResourceLocation}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{CreativeModeTab, Item}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.storage.loot.{LootPool, LootTable}
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.LootTableLoadEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import net.neoforged.neoforge.registries.RegisterEvent
import skylands.SkylandsCommon.ModId
import skylands.block.CloudBlock
import skylands.neoforge.block.CloudBlockNeoForge
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier
import scala.collection.mutable

// NeoForge impl: queue registrations per registry and flush on RegisterEvent;
// runtime events go on NeoForge.EVENT_BUS. Client render-type wiring lives on
// the mod bus via FMLClientSetupEvent.
class NeoForgePlatform(modBus: IEventBus) extends SkylandsPlatform:
  import NeoForgePlatform._

  private val pendingBlocks =
    mutable.ArrayBuffer.empty[(String, () => Block, LazyRef[Block])]
  private val pendingItems =
    mutable.ArrayBuffer.empty[(String, () => Item, LazyRef[Item])]
  private val pendingBlockEntityTypes =
    mutable.ArrayBuffer.empty[(String, () => BlockEntityType[?], LazyRef[BlockEntityType[?]])]
  private val pendingCreativeTabs =
    mutable.ArrayBuffer.empty[(String, () => CreativeModeTab, LazyRef[CreativeModeTab])]
  private val pendingChunkGenerators =
    mutable.ArrayBuffer.empty[(String, MapCodec[? <: ChunkGenerator])]

  private val lootMods =
    mutable.ArrayBuffer.empty[(java.util.Set[ResourceKey[LootTable]], () => LootPool.Builder)]
  private val playerTicks = mutable.ArrayBuffer.empty[Player => Unit]
  private val translucentBlocks = mutable.ArrayBuffer.empty[Supplier[? <: Block]]

  // --- mod event bus wiring --------------------------------------------------

  modBus.addListener { (e: RegisterEvent) =>
    val key = e.getRegistryKey
    if key == Registries.BLOCK then flushBlocks(e)
    else if key == Registries.ITEM then flushItems(e)
    else if key == Registries.BLOCK_ENTITY_TYPE then flushBlockEntityTypes(e)
    else if key == Registries.CREATIVE_MODE_TAB then flushCreativeTabs(e)
    else if key == Registries.CHUNK_GENERATOR then flushChunkGenerators(e)
  }

  if FMLEnvironment.dist == Dist.CLIENT then
    modBus.addListener { (_: FMLClientSetupEvent) =>
      translucentBlocks.foreach(s =>
        ItemBlockRenderTypes.setRenderLayer(s.get(), RenderType.translucent())
      )
    }

  // --- common event bus wiring -----------------------------------------------

  NeoForge.EVENT_BUS.addListener { (e: PlayerTickEvent.Post) =>
    val p = e.getEntity
    playerTicks.foreach(cb => cb(p))
  }

  NeoForge.EVENT_BUS.addListener { (e: LootTableLoadEvent) =>
    val key = ResourceKey.create(Registries.LOOT_TABLE, e.getName)
    lootMods.foreach { case (targets, factory) =>
      if targets.contains(key) then e.getTable.addPool(factory().build())
    }
  }

  // --- SPI -------------------------------------------------------------------

  override def registerBlock[B <: Block](id: String, factory: () => B): Supplier[B] =
    val slot = new LazyRef[B]
    pendingBlocks.append(
      (id, factory.asInstanceOf[() => Block], slot.asInstanceOf[LazyRef[Block]])
    )
    slot

  override def newCloudBlock(): CloudBlock = new CloudBlockNeoForge()

  override def registerItem[I <: Item](id: String, factory: () => I): Supplier[I] =
    val slot = new LazyRef[I]
    pendingItems.append(
      (id, factory.asInstanceOf[() => Item], slot.asInstanceOf[LazyRef[Item]])
    )
    slot

  override def registerBlockEntityType[T <: BlockEntity](
      id: String,
      factory: () => BlockEntityType[T]
  ): Supplier[BlockEntityType[T]] =
    val slot = new LazyRef[BlockEntityType[T]]
    pendingBlockEntityTypes.append(
      (
        id,
        factory.asInstanceOf[() => BlockEntityType[?]],
        slot.asInstanceOf[LazyRef[BlockEntityType[?]]]
      )
    )
    slot

  override def registerCreativeTab(
      id: String,
      factory: () => CreativeModeTab
  ): Supplier[CreativeModeTab] =
    val slot = new LazyRef[CreativeModeTab]
    pendingCreativeTabs.append((id, factory, slot))
    slot

  override def registerChunkGenerator(id: String, codec: MapCodec[? <: ChunkGenerator]): Unit =
    pendingChunkGenerators.append((id, codec))

  override def onPlayerPostTick(callback: Player => Unit): Unit =
    playerTicks.append(callback)

  override def onLootTableModify(
      targets: java.util.Set[ResourceKey[LootTable]],
      poolFactory: () => LootPool.Builder
  ): Unit =
    lootMods.append((targets, poolFactory))

  override def setTranslucentRenderType(block: Supplier[? <: Block]): Unit =
    translucentBlocks.append(block)

  // --- flush helpers ---------------------------------------------------------

  private def rl(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(ModId, path)

  private def flushBlocks(e: RegisterEvent): Unit =
    pendingBlocks.foreach { case (id, factory, slot) =>
      val b = factory()
      e.register(Registries.BLOCK, rl(id), () => b)
      slot.set(b)
    }

  private def flushItems(e: RegisterEvent): Unit =
    pendingItems.foreach { case (id, factory, slot) =>
      val i = factory()
      e.register(Registries.ITEM, rl(id), () => i)
      slot.set(i)
    }

  private def flushBlockEntityTypes(e: RegisterEvent): Unit =
    pendingBlockEntityTypes.foreach { case (id, factory, slot) =>
      val be = factory()
      e.register(Registries.BLOCK_ENTITY_TYPE, rl(id), () => be)
      slot.set(be)
    }

  private def flushCreativeTabs(e: RegisterEvent): Unit =
    pendingCreativeTabs.foreach { case (id, factory, slot) =>
      val tab = factory()
      e.register(Registries.CREATIVE_MODE_TAB, rl(id), () => tab)
      slot.set(tab)
    }

  private def flushChunkGenerators(e: RegisterEvent): Unit =
    pendingChunkGenerators.foreach { case (id, codec) =>
      e.register(Registries.CHUNK_GENERATOR, rl(id), () => codec)
    }

object NeoForgePlatform:
  // Late-bound Supplier — consumers of registerX() get this; it resolves when
  // our RegisterEvent listener fires.
  private final class LazyRef[T] extends Supplier[T]:
    @volatile private var value: T | Null = null
    def set(v: T): Unit = value = v
    override def get(): T =
      val v = value
      if v == null then
        throw new IllegalStateException("Skylands registry entry accessed before RegisterEvent fired")
      v.asInstanceOf[T]
