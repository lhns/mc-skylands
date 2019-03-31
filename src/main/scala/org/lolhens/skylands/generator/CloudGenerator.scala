package org.lolhens.skylands.generator

import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.ops.BlockPosOps._
import org.lolhens.skylands.world.BlockArray

import scala.util.Random

/**
  * Created by pierr on 20.01.2017.
  */
class CloudGenerator(world: WorldServer, position: BlockPos) extends StructureGenerator(world, position) {
  val syncedWorld: BlockArray = BlockArray.syncVertical(
    world,
    world.getMinecraftServer.getWorld(SkylandsMod.skylands.skylandsDimensionType.getId),
    15
  )

  val random = new Random(world.getSeed + position.getX + position.getZ * position.getY)

  def drawCloudBlock(position: BlockPos): Unit =
    if (syncedWorld.isReplaceable(position))
      syncedWorld.setBlockState(position, SkylandsMod.skylands.blockCloud.getDefaultState)

  def drawSphere(position: BlockPos, size: Int): Unit = {
    for (
      x <- -size to size;
      y <- -size to size;
      z <- -size to size
    ) {
      if (new BlockPos(x, y, z).inSphere(size))
        drawCloudBlock(position.add(x, y, z))
    }
  }

  def drawCloud(position: BlockPos, size: Int): Unit = {
    for (_ <- 0 until size * size) {
      val (x, z) = (random.nextInt(size * 2) - size, random.nextInt(size * 2) - size)
      if (new BlockPos(x, 0, z).inSphere(size)) drawSphere(position.add(x, random.nextInt(2) - 1, z), random.nextInt(8) + 2)
    }
  }

  drawCloud(position, 30)

  override def update(): Unit = ()
}
