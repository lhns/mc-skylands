package org.lolhens.skylands.generator

import net.minecraft.block.BlockState
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import net.minecraft.world.server.ServerWorld
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.block.BlockBeanStem
import org.lolhens.skylands.enrich.RichBlockPos._
import org.lolhens.skylands.world.BlockArray

/**
  * Created by pierr on 15.01.2017.
  */
class BeanstalkGenerator(world: ServerWorld, position: BlockPos) extends StructureGenerator(world, position) {
  private val syncedWorld: BlockArray = BlockArray.syncVertical(
    world,
    world.getServer.getWorld(SkylandsMod.skylands.skylandsDimensionType.getId),
    SkylandsMod.skylands.skylandsOverlap
  )

  private val perlinNoiseSLOctaves = 7
  private val amplitudeMax = 40.0
  private val octaveDecrease = 1.12
  private val minSineFreqDivider = 30.0
  private val sineFreqDividerDecrease = 0.66
  private val maxSingleOffsetX = 30
  private val maxSingleOffsetZ = 30

  private val sineLayerAmplitudes = Seq.tabulate(perlinNoiseSLOctaves)(i => (scala.util.Random.nextDouble() - 0.5) * amplitudeMax * 2.0 * (1.0 / math.pow(octaveDecrease, i)))
  private val sineLayerOffset = Seq.tabulate(perlinNoiseSLOctaves)(i => scala.util.Random.nextDouble() * 2 * math.Pi * (minSineFreqDivider - math.pow(sineFreqDividerDecrease, i)))

  private var progress: Int = 0
  private var lastBlockPos: BlockPos = position

  private def drawBlock(position: BlockPos, blockState: BlockState) =
    if (syncedWorld.isReplaceable(position) ||
      syncedWorld.isTerrainBlock(position))
      syncedWorld.setBlockState(position, blockState)

  private def drawLayer(destinationPosition: BlockPos) = {
    val direction = destinationPosition.subtract(lastBlockPos)
    val rdDirection = direction.add(world.rand.nextInt(maxSingleOffsetX) - (maxSingleOffsetX / 2), 0, world.rand.nextInt(maxSingleOffsetZ) - maxSingleOffsetZ / 2)

    val rdLength = Math.sqrt(Seq(rdDirection.getX * rdDirection.getX, rdDirection.getY * rdDirection.getY, rdDirection.getZ * rdDirection.getZ).max)
    val rdDirectionNorm = new BlockPos((rdDirection.getX / rdLength).round, 1, (rdDirection.getZ / rdLength).round)

    lastBlockPos = lastBlockPos.add(rdDirectionNorm)

    drawBlock(lastBlockPos, SkylandsMod.skylands.blockBeanStem.getDefaultState.withProperty(BlockBeanStem.isCenter, Boolean.box(true)))

    def recursiveFunction(currentPos: BlockPos, steps: Int = 0): Unit = {
      if (steps > 600) return
      val size = math.log1p(steps * 1.4).toInt

      for (x <- -size to size; z <- -size to size)
        if (new BlockPos(x, 0, z).inSphere(size + 0.1))
          drawBlock(currentPos.add(x, 0, z), SkylandsMod.skylands.blockBeanStem.getDefaultState)

      if (currentPos.getY >= position.getY) {
        for (x <- -1 to 1; z <- -1 to 1) {
          val newPos = currentPos.add(x, -1, z)
          val bs = syncedWorld.getBlockState(newPos)
          if (bs.getBlock == SkylandsMod.skylands.blockBeanStem && bs.getValue(BlockBeanStem.isCenter) && newPos != currentPos)
            recursiveFunction(newPos, steps + 1)
        }
      }
    }

    recursiveFunction(lastBlockPos)

    if (lastBlockPos.getY >= 245 && lastBlockPos.getY <= 255)
      new CloudGenerator(world, lastBlockPos)
  }

  override def update(): Unit = {
    val xOffset = sineLayerAmplitudes.zip(sineLayerOffset).zipWithIndex.map {
      case ((ampl, offset), index) =>
        //println((ampl, offset, index))
        Math.sin(progress.toDouble / (minSineFreqDivider - math.pow(sineFreqDividerDecrease, index)) + offset) * ampl
    }.sum

    val zOffset = sineLayerAmplitudes.zip(sineLayerOffset).zipWithIndex.map {
      case ((ampl, offset), index) =>
        Math.cos(progress.toDouble / (minSineFreqDivider - math.pow(sineFreqDividerDecrease, index)) + offset) * ampl
    }.sum

    val destinationPosition = position.add(xOffset, progress + 3, zOffset)

    if (destinationPosition.getY > 430)
      world.setBlockState(position, SkylandsMod.skylands.blockBeanStem.getDefaultState)

    drawLayer(destinationPosition)

    progress += 1
  }
}
