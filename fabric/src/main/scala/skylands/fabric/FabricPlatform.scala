package skylands.fabric

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.{ResourceKey, ResourceLocation}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{CreativeModeTab, Item}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.storage.loot.{LootPool, LootTable}
import skylands.SkylandsCommon.ModId
import skylands.block.CloudBlock
import skylands.platform.SkylandsPlatform

import java.util.function.Supplier

// Fabric impl: registrations call BuiltInRegistries directly and return the
// registered instance wrapped in a Supplier. Events wire to fabric-api.
class FabricPlatform extends SkylandsPlatform:
  private def rl(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(ModId, path)

  override def registerBlock[B <: Block](id: String, factory: () => B): Supplier[B] =
    val block: B = Registry.register(BuiltInRegistries.BLOCK, rl(id), factory())
    () => block

  override def newCloudBlock(): CloudBlock = new CloudBlock()

  override def registerItem[I <: Item](id: String, factory: () => I): Supplier[I] =
    val item: I = Registry.register(BuiltInRegistries.ITEM, rl(id), factory())
    () => item

  override def registerBlockEntityType[T <: BlockEntity](
      id: String,
      factory: () => BlockEntityType[T]
  ): Supplier[BlockEntityType[T]] =
    val be: BlockEntityType[T] =
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl(id), factory())
    () => be

  override def registerCreativeTab(id: String, factory: () => CreativeModeTab): Supplier[CreativeModeTab] =
    val tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, rl(id), factory())
    () => tab

  override def registerChunkGenerator(id: String, codec: MapCodec[? <: ChunkGenerator]): Unit =
    Registry.register(BuiltInRegistries.CHUNK_GENERATOR, rl(id), codec)

  override def onPlayerPostTick(callback: Player => Unit): Unit =
    ServerTickEvents.END_SERVER_TICK.register(server =>
      server.getPlayerList.getPlayers.forEach(p => callback(p))
    )

  override def onLootTableModify(
      targets: java.util.Set[ResourceKey[LootTable]],
      poolFactory: () => LootPool.Builder
  ): Unit =
    LootTableEvents.MODIFY.register((key, tableBuilder, _, _) =>
      if targets.contains(key) then tableBuilder.withPool(poolFactory())
    )

  override def setTranslucentRenderType(block: Supplier[? <: Block]): Unit =
    BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.translucent())
