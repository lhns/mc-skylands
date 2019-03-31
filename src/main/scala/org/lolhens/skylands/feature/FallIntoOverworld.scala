package org.lolhens.skylands.feature

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraft.world.{DimensionType, Teleporter, World}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.world.SimpleTeleporter
import org.lolhens.skylands.ops.EntityOps._

/**
  * Created by pierr on 15.01.2017.
  */
object FallIntoOverworld {
  def update(player: Entity): Unit = {
    val teleportTarget: Option[(DimensionType, Vec3d)] =
      if (player.dimension == DimensionType.OVERWORLD.getId && player.posY >= 250) {
        def isNearBeanStem(world: World, pos: BlockPos, radius: Int) = {
          val positions = for (
            x <- -radius to radius;
            z <- -radius to radius
          ) yield pos.add(x, 0, z)

          positions.exists(position => world.getBlockState(position).getBlock == SkylandsMod.skylands.blockBeanStem)
        }

        /*if (isNearBeanStem(player.world, new BlockPos(player), 4))
          Some((skylandsDimensionType.getId, new BlockPos(player.posX, 10, player.posZ)))
        else*/
        None
      } else if (player.dimension == SkylandsMod.skylands.skylandsDimensionType.getId && player.posY <= 5)
        Some((DimensionType.OVERWORLD, new Vec3d(player.posX, 245, player.posZ)))
      else
        None

    for ((dimension, position) <- teleportTarget) {
      //val teleporter: Teleporter = new SimpleTeleporter(player.getServer.getWorld(dimensionId), Some(position))
      //player.getServer.getPlayerList.transferPlayerToDimension(player, dimensionId, teleporter)
      player.teleportTo(dimension, position, Vec3d.ZERO)
    }
  }
}
