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
import org.lolhens.skylands.block.{BlockBean, BlockBeanStem, BlockCloud, BlockPortal}
import org.lolhens.skylands.tileentities.TileEntityBeanPlant
import org.lolhens.skylands.world.{SimpleTeleporter, WorldProviderSkylands}

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val blockPortal = new BlockPortal()
  GameRegistry.register(blockPortal.setRegistryName("portal"))
  GameRegistry.register(new ItemBlock(blockPortal).setRegistryName(blockPortal.getRegistryName))

  val blockBeanStem = new BlockBeanStem()
  GameRegistry.register(blockBeanStem.setRegistryName("beanstem"))
  GameRegistry.register(new ItemBlock(blockBeanStem).setRegistryName(blockBeanStem.getRegistryName))

  val blockBean = new BlockBean()
  GameRegistry.register(blockBean.setRegistryName("bean"))
  GameRegistry.register(new ItemBlock(blockBean).setRegistryName(blockBean.getRegistryName))
  GameRegistry.registerTileEntity(classOf[TileEntityBeanPlant], "bean_tile_entity")

  val blockCloud = new BlockCloud()
  GameRegistry.register(blockCloud.setRegistryName("cloud"))
  GameRegistry.register(new ItemBlock(blockCloud).setRegistryName(blockCloud.getRegistryName))

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

              positions.exists(position => world.getBlockState(position).getBlock == blockBeanStem)
            }

            /*if (isNearBeanStem(player.world, new BlockPos(player), 4))
              Some((skylandsDimensionType.getId, new BlockPos(player.posX, 10, player.posZ)))
            else*/
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

    if (player.dimension == skylandsDimensionType.getId && heldItem.contains(Items.FEATHER) && !player.capabilities.isFlying) {
      if (player.motionY <= -0.3) {
        player.jumpMovementFactor = 0.1f
        player.motionY = Math.max(player.motionY, -0.3)
      }
      player.fallDistance = 0
    }
  }
}
