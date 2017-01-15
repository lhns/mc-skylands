package org.lolhens.skylands.generator

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{World, WorldServer}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.block.BlockBeanStem

/**
  * Created by pierr on 15.01.2017.
  */
class BeanstalkGenerator(world: WorldServer, position: BlockPos) extends StructureGenerator(world, position) {
  private def worldSkylands = world.getMinecraftServer.worldServerForDimension(SkylandsMod.skylands.skylandsDimensionType.getId)

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

  private def isReplaceable(world: World, pos: BlockPos): Boolean = {
    val blockState = world.getBlockState(pos)

    blockState.getMaterial.isReplaceable
    blockState.getBlock.isLeaves(blockState, world, pos) ||
      Seq(Material.AIR, Material.LEAVES).contains(blockState.getMaterial) ||
      Seq(Blocks.GRASS, Blocks.DIRT, Blocks.SAPLING, Blocks.VINE).contains(blockState.getBlock)
  }

  private def drawBlock(position: BlockPos, blockState: IBlockState) = {
    if (position.getY <= 255)
      if (isReplaceable(world, position) || world.getBlockState(position).getBlock == SkylandsMod.skylands.blockCloud)
        world.setBlockState(position, blockState)
    if (position.getY >= 245) {
      val skyPosition = position.add(0, -240, 0)
      if (isReplaceable(worldSkylands, skyPosition) || worldSkylands.getBlockState(skyPosition).getBlock == SkylandsMod.skylands.blockCloud)
        worldSkylands.setBlockState(skyPosition, blockState)
    }
  }

  private def getBlock(position: BlockPos) = {
    if (position.getY <= 255)
      world.getBlockState(position)
    else {
      val skyPosition = position.add(0, -240, 0)
      worldSkylands.getBlockState(skyPosition)
    }
  }

  private def drawLayer(destinationPosition: BlockPos) = {
    val direction = destinationPosition.subtract(lastBlockPos)
    val rdDirection = direction.add(world.rand.nextInt(maxSingleOffsetX) - (maxSingleOffsetX / 2), 0, world.rand.nextInt(maxSingleOffsetZ) - maxSingleOffsetZ / 2)

    val rdLength = Math.sqrt(Seq(rdDirection.getX * rdDirection.getX, rdDirection.getY * rdDirection.getY, rdDirection.getZ * rdDirection.getZ).max)
    val rdDirectionNorm = new BlockPos((rdDirection.getX / rdLength).round, 1, (rdDirection.getZ / rdLength).round)

    //println(lastBlockPos)

    lastBlockPos = lastBlockPos.add(rdDirectionNorm)

    drawBlock(lastBlockPos, SkylandsMod.skylands.blockBeanStem.getDefaultState.withProperty(BlockBeanStem.isCenter, Boolean.box(true)))

    def recursiveFunction(currentPos: BlockPos, steps: Int = 0): Unit = {
      if (steps > 500) return
      val size = math.log1p(steps * 1.4).toInt

      for (x <- -size to size; z <- -size to size) {
        if (Math.sqrt(x * x + z * z) <= size) {
          drawBlock(currentPos.add(x, 0, z), SkylandsMod.skylands.blockBeanStem.getDefaultState)
        }
      }

      if (currentPos.getY >= position.getY) {
        for (x <- -1 to 1; z <- -1 to 1) {
          val newPos = currentPos.add(x, -1, z)
          val bs = getBlock(newPos)
          if (bs.getBlock == SkylandsMod.skylands.blockBeanStem && bs.getValue(BlockBeanStem.isCenter) && newPos != currentPos) {
            recursiveFunction(newPos, steps + 1)
          }
        }
      }
    }

    recursiveFunction(lastBlockPos)

    if (lastBlockPos.getY >= 240 && lastBlockPos.getY <= 255) {
      val size = 30
      for (x <- -size to size; z <- -size to size) {
        if (Math.sqrt(x * x + z * z) <= size) {
          drawBlock(lastBlockPos.add(x, 0, z), SkylandsMod.skylands.blockCloud.getDefaultState)
        }
      }
    }
  }

  override def update(): Unit = {
    //if (progress == 0) SkylandsMod.skylands.skylandsDimensionType.setLoadSpawn(true)
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

    if (destinationPosition.getY > 350) {
      world.setBlockState(position, SkylandsMod.skylands.blockBeanStem.getDefaultState)
      //SkylandsMod.skylands.skylandsDimensionType.setLoadSpawn(false)
    }

    drawLayer(destinationPosition)

    progress += 1
  }
}
