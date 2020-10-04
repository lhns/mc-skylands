package de.lolhens.minecraft.skylandsmod

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import de.lolhens.minecraft.skylandsmod.block.{BeanBlock, BeanstalkBlock, CloudBlock}
import de.lolhens.minecraft.skylandsmod.config.SkylandsConfig
import de.lolhens.minecraft.skylandsmod.dimension.SkylandsChunkGenerator
import net.fabricmc.api.{ClientModInitializer, ModInitializer}
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.block.Block
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.{BlockItem, Item, ItemGroup}
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import net.minecraft.util.registry.{Registry, RegistryKey}
import net.minecraft.world.World

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

object SkylandsMod extends ModInitializer with ClientModInitializer {
  val metadata: ModMetadata = {
    FabricLoader.getInstance().getEntrypointContainers("main", classOf[ModInitializer])
      .iterator().asScala.find(this eq _.getEntrypoint).get.getProvider.getMetadata
  }

  lazy val config: SkylandsConfig = SkylandsConfig.loadOrCreate(metadata.getId)

  val CLOUD_BLOCK_ID = new Identifier(metadata.getId, "cloud")
  val CLOUD_BLOCK: Block = new CloudBlock()

  val BEAN_BLOCK_ID = new Identifier(metadata.getId, "bean")
  val BEAN_BLOCK: Block = new BeanBlock()

  val BEANSTALK_BLOCK_ID = new Identifier(metadata.getId, "beanstalk")
  val BEANSTALK_BLOCK: Block = new BeanstalkBlock()

  val SKYLANDS: RegistryKey[World] = RegistryKey.of(Registry.DIMENSION, new Identifier(metadata.getId, "skylands"))

  override def onInitialize(): Unit = {
    config

    Registry.register(Registry.BLOCK, CLOUD_BLOCK_ID, CLOUD_BLOCK)
    Registry.register(Registry.ITEM, CLOUD_BLOCK_ID, new BlockItem(CLOUD_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)))

    Registry.register(Registry.BLOCK, BEAN_BLOCK_ID, BEAN_BLOCK)
    Registry.register(Registry.ITEM, BEAN_BLOCK_ID, new BlockItem(BEAN_BLOCK, new Item.Settings().group(ItemGroup.MISC)))

    Registry.register(Registry.BLOCK, BEANSTALK_BLOCK_ID, BEANSTALK_BLOCK)
    Registry.register(Registry.ITEM, BEANSTALK_BLOCK_ID, new BlockItem(BEANSTALK_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)))

    Registry.register(Registry.CHUNK_GENERATOR, new Identifier(metadata.getId, "skylands"), SkylandsChunkGenerator.CODEC)

    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) =>
      dispatcher.register(literal("skylands").executes(skylandsCommand(_)))
    )
  }

  @throws[CommandSyntaxException]
  private def skylandsCommand(context: CommandContext[ServerCommandSource]): Int = {
    try {
      val serverPlayerEntity = context.getSource.getPlayer
      val serverWorld = serverPlayerEntity.getServerWorld
      if (!(serverWorld.getRegistryKey == SKYLANDS))
        serverPlayerEntity.teleport(context.getSource.getMinecraftServer.getWorld(SKYLANDS), 0, 255, 0, 0, 0)
      else
        serverPlayerEntity.teleport(context.getSource.getMinecraftServer.getWorld(World.OVERWORLD), 0, 255, 0, 0, 0)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
    }
    1
  }

  override def onInitializeClient(): Unit = {
    BlockRenderLayerMap.INSTANCE.putBlock(CLOUD_BLOCK, RenderLayer.getTranslucent)
  }
}
