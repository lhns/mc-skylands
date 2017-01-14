package org.lolhens.skylands

import java.io.File

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{DimensionType, Teleporter, World}
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lolhens.skylands.block.{BlockBean, BlockBeanStem, BlockPortal}
import org.lolhens.skylands.tileentities.TileEntityBeanPlant
import org.lolhens.skylands.world.{SimpleTeleporter, WorldProviderSkylands}

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val portal = new BlockPortal()
  GameRegistry.register(portal.setRegistryName("portal"))
  GameRegistry.register(new ItemBlock(portal).setRegistryName(portal.getRegistryName))

  val beanstem = new BlockBeanStem()
  GameRegistry.register(beanstem.setRegistryName("beanstem"))
  GameRegistry.register(new ItemBlock(beanstem).setRegistryName(beanstem.getRegistryName))

  val bean = new BlockBean()
  GameRegistry.register(bean.setRegistryName("bean"))
  GameRegistry.register(new ItemBlock(bean).setRegistryName(bean.getRegistryName))
  GameRegistry.registerTileEntity(classOf[TileEntityBeanPlant], "bean_tile_entity")

  val skylandsDimensionType: DimensionType = DimensionType.register("Skylands", "sky", config.dimensionId, classOf[WorldProviderSkylands], false)
  DimensionManager.registerDimension(config.dimensionId, skylandsDimensionType)

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  def onPlayerTick(event: TickEvent.PlayerTickEvent): Unit = {
    val player = event.player

    player match {
      case player: EntityPlayerMP =>
        val teleportTarget: Option[(Int, BlockPos)] =
          if (player.dimension == DimensionType.OVERWORLD.getId && player.posY >= 250) {
            def isNearBeanStem(world: World, pos: BlockPos, radius: Int) = {
              val positions = for (
                x <- -radius to radius;
                z <- -radius to radius
              ) yield pos.add(x, 0, z)

              positions.exists(position => world.getBlockState(position).getBlock == beanstem)
            }

            if (isNearBeanStem(player.world, new BlockPos(player), 4))
              Some((skylandsDimensionType.getId, new BlockPos(player.posX, 10, player.posZ)))
            else
              None
          } else if (player.dimension == skylandsDimensionType.getId && player.posY <= 5)
            Some((DimensionType.OVERWORLD.getId, new BlockPos(player.posX, 245, player.posZ)))
          else
            None

        for ((dimensionId, position) <- teleportTarget) {
          val teleporter: Teleporter = new SimpleTeleporter(player.getServer.worldServerForDimension(dimensionId), Some(position))
          player.getServer.getPlayerList.transferPlayerToDimension(player, dimensionId, teleporter)
        }

      case _ =>
    }

    val heldItem = Option(player.getHeldItemMainhand).map(_.getItem)

    if (heldItem.contains(Items.FEATHER) && !player.capabilities.isCreativeMode) {
      println(player.jumpMovementFactor)
      player.motionY = Math.max(player.motionY, -0.3)
      player.fallDistance = 0
    }
  }
}
