package org.lolhens.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import org.lolhens.skylands.SkylandsMod

/**
  * Created by pierr on 01.01.2017.
  */
class SkylandsTeleporter(world: WorldServer, position: BlockPos) extends AbstractTeleporter(world) {
  override def teleport(entity: Entity, rotationYaw: Float): Unit = {
    val skylandsDimensionId = SkylandsMod.skylands.skylandsDimensionType.getId

    val portalPosition = world.provider.getDimension match {
      case `skylandsDimensionId` => // Teleporting to skylands
        println("to the skylands")
        val portalPosition = new BlockPos(position.getX, 64, position.getZ)
        if (world.getBlockState(portalPosition).getBlock != SkylandsMod.skylands.blockPortal) {
          for (
            x <- -3 to 3;
            z <- -3 to 3;
            pos = portalPosition.add(x, 0, z)
            if world.isAirBlock(pos)
          ) world.setBlockState(pos, Blocks.STONE.getDefaultState)

          world.setBlockState(portalPosition, SkylandsMod.skylands.blockPortal.getDefaultState)
        }
        portalPosition

      case dimensionId => entity match {
        case player: EntityPlayer => // Teleporting back
          println("and back")
          val yColumn: Seq[BlockPos] = for (y <- 0 until 256) yield new BlockPos(position.getX, y, position.getZ)
          val portalPosition: Option[BlockPos] = yColumn.find(pos => world.getBlockState(pos).getBlock == SkylandsMod.skylands.blockPortal)
          val spawnPoint: BlockPos = portalPosition.map(_.add(0, 1, 0))
            .orElse(Option(player.getBedLocation(dimensionId)))
            .getOrElse(world.provider.getRandomizedSpawnPoint)
          spawnPoint

        case _ =>
          position
      }
    }

    entity.setLocationAndAngles(portalPosition.getX + 0.5, portalPosition.getY + 1, portalPosition.getZ + 0.5, entity.rotationYaw, 0)
    entity.motionX = 0
    entity.motionY = 0
    entity.motionZ = 0
  }
}
