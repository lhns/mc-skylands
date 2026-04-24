package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import skylands.registry.{SkylandsBlocks, SkylandsWorldgen}
import skylands.util.*

import scala.util.Random

// Faithful port of 1.12.2 CloudGenerator. Scatters clusters of cloud spheres
// inside a radius=30 disc around the beanstalk-top position, syncing writes
// across the Overworld/Skylands seam.
class CloudGenerator(level: ServerLevel, position: BlockPos) extends StructureGenerator(level, position):
  private val SkylandsOverlap = 15
  private val seamY: Int = level.getMaxBuildHeight - 1

  private val syncedWorld: BlockArray = {
    val skylands = level.getServer.getLevel(SkylandsWorldgen.SKYLANDS_LEVEL)
    if skylands == null then BlockArray.forLevel(level)
    else BlockArray.syncVertical(level, skylands, SkylandsOverlap, seamY)
  }

  private val random = new Random(level.getSeed + position.getX + position.getZ.toLong * position.getY.toLong)

  private def drawCloudBlock(p: BlockPos): Unit =
    if syncedWorld.isReplaceable(p) then
      syncedWorld.setBlockState(p, SkylandsBlocks.CLOUD.get().defaultBlockState())

  private def drawSphere(p: BlockPos, size: Int): Unit =
    var x = -size
    while x <= size do
      var y = -size
      while y <= size do
        var z = -size
        while z <= size do
          if new BlockPos(x, y, z).inSphere(size) then
            drawCloudBlock(p.offset(x, y, z))
          z += 1
        y += 1
      x += 1

  private def drawCloud(p: BlockPos, size: Int): Unit =
    val attempts = size * size
    var i = 0
    while i < attempts do
      val x = random.nextInt(size * 2) - size
      val z = random.nextInt(size * 2) - size
      if new BlockPos(x, 0, z).inSphere(size) then
        drawSphere(p.offset(x, random.nextInt(2) - 1, z), random.nextInt(8) + 2)
      i += 1

  drawCloud(position, 30)

  override def update(): Unit = ()
