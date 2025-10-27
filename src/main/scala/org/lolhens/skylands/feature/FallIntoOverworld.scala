package org.lolhens.skylands.feature

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{DimensionType, Teleporter, World}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.world.SimpleTeleporter

/**
  * Created by pierr on 15.01.2017.
  */
object FallIntoOverworld {
  def update(player: EntityPlayerMP): Unit = {
    val toSkylands: Option[BlockPos] =
      if (player.dimension != SkylandsMod.skylands.skylandsDimensionType.getId && player.posY > 256)
        Some(new BlockPos(player.posX, 257, player.posZ))
      else
        None

    val toOverworld: Option[BlockPos] =
      if (player.dimension == SkylandsMod.skylands.skylandsDimensionType.getId && player.posY <= 5)
        Some(new BlockPos(player.posX, 245, player.posZ))
      else
        None

    // teleport to Skylands
    for (target <- toSkylands) {
      val serverWorld = player.getServer.getWorld(SkylandsMod.skylands.skylandsDimensionType.getId)
      // find a safe spawn on top of terrain at the same X/Z
      val basePos = new BlockPos(target.getX, 0, target.getZ)
      val top = serverWorld.getTopSolidOrLiquidBlock(basePos)
      val spawnPos = if (top.getY <= 0) new BlockPos(target.getX, 10, target.getZ) else top.up()
      val teleporter: Teleporter = new SimpleTeleporter(serverWorld, Some(spawnPos))
      player.getServer.getPlayerList.transferPlayerToDimension(player, SkylandsMod.skylands.skylandsDimensionType.getId, teleporter)
    }

    // teleport back to overworld
    for (target <- toOverworld) {
      val serverWorld = player.getServer.getWorld(DimensionType.OVERWORLD.getId)
      val top = serverWorld.getTopSolidOrLiquidBlock(new BlockPos(target.getX, 0, target.getZ))
      val spawnPos = if (top.getY <= 0) new BlockPos(target.getX, 245, target.getZ) else top.up()
      val teleporter: Teleporter = new SimpleTeleporter(serverWorld, Some(spawnPos))
      player.getServer.getPlayerList.transferPlayerToDimension(player, DimensionType.OVERWORLD.getId, teleporter)
    }
  }
}
