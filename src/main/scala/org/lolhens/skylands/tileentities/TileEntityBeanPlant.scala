package org.lolhens.skylands.tileentities

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{World, WorldServer}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.block.BlockBeanStem

/**
  * Created by pierr on 14.01.2017.
  */
class TileEntityBeanPlant extends TileEntity with ITickable {
  private var progress: Int = 0
  private var lastBlockPos: BlockPos = _

  private val perlinNoiseSLOctaves = 7
  private val amplitudeMax = 40.0
  private val octaveDecrease = 1.12
  private val minSineFreqDivider = 30.0
  private val sineFreqDividerDecrease = 0.66
  private val maxSingleOffsetX = 30
  private val maxSingleOffsetZ = 30

  private val sineLayerAmplitudes = Seq.tabulate(perlinNoiseSLOctaves)(i => (scala.util.Random.nextDouble() - 0.5) * amplitudeMax * 2.0 * (1.0 / math.pow(octaveDecrease, i)))
  private val sineLayerOffset = Seq.tabulate(perlinNoiseSLOctaves)(i => scala.util.Random.nextDouble() * 2 * math.Pi * (minSineFreqDivider - math.pow(sineFreqDividerDecrease, i)))

  override def update(): Unit = {
    world match {
      case world: WorldServer =>
        val worldSkylands = world.getMinecraftServer.worldServerForDimension(SkylandsMod.skylands.skylandsDimensionType.getId)

        if (lastBlockPos == null) lastBlockPos = pos

        def drawBlock(position: BlockPos, blockState: IBlockState, isCenter: Boolean = false) = {
          if (position.getY < 255)
            if (isReplaceable(world, position))
              world.setBlockState(position, blockState.withProperty(BlockBeanStem.isCenter, isCenter.asInstanceOf[java.lang.Boolean]))
          if (position.getY >= 240) {
            val skyPosition = position.add(0, -240, 0)
            if (isReplaceable(worldSkylands, skyPosition))
              worldSkylands.setBlockState(skyPosition, blockState.withProperty(BlockBeanStem.isCenter, isCenter.asInstanceOf[java.lang.Boolean]))
          }
        }

        def getBlock(position: BlockPos) = {
          if (position.getY < 255)
            world.getBlockState(position)
          else {
            val skyPosition = position.add(0, -240, 0)
            worldSkylands.getBlockState(skyPosition)
          }
        }

        def drawLayer(destinationPosition: BlockPos) = {
          val direction = destinationPosition.subtract(lastBlockPos)
          val rdDirection = direction.add(world.rand.nextInt(maxSingleOffsetX) - (maxSingleOffsetX / 2), 0, world.rand.nextInt(maxSingleOffsetZ) - maxSingleOffsetZ / 2)

          val rdLength = Math.sqrt(Seq(rdDirection.getX * rdDirection.getX, rdDirection.getY * rdDirection.getY, rdDirection.getZ * rdDirection.getZ).max)
          val rdDirectionNorm = new BlockPos((rdDirection.getX / rdLength).round, 1, (rdDirection.getZ / rdLength).round)

          println(lastBlockPos)

          lastBlockPos = lastBlockPos.add(rdDirectionNorm)

          drawBlock(lastBlockPos, SkylandsMod.skylands.beanstem.getDefaultState, isCenter = true)

          def recursiveFunction(currentPos: BlockPos, steps: Int = 0): Unit = {
            val size = math.log1p(steps * 1.4).toInt

            for (x <- -size to size; z <- -size to size) {
              if (Math.sqrt(x * x + z * z) <= size) {
                drawBlock(currentPos.add(x, 0, z), SkylandsMod.skylands.beanstem.getDefaultState)
              }
            }

            if (currentPos.getY >= pos.getY) {
              for (x <- -1 to 1; z <- -1 to 1) {
                val newPos = currentPos.add(x, -1, z)
                val bs = getBlock(newPos)
                if (bs.getBlock == SkylandsMod.skylands.beanstem && bs.getValue(BlockBeanStem.isCenter) && newPos != currentPos) {
                  recursiveFunction(newPos, steps + 1)
                }
              }
            }
          }

          recursiveFunction(lastBlockPos)
        }

        val xOffset = sineLayerAmplitudes.zip(sineLayerOffset).zipWithIndex.map {
          case ((ampl, offset), index) =>
            //println((ampl, offset, index))
            Math.sin(progress.toDouble / (minSineFreqDivider - math.pow(sineFreqDividerDecrease, index)) + offset) * ampl
        }.sum

        val zOffset = sineLayerAmplitudes.zip(sineLayerOffset).zipWithIndex.map {
          case ((ampl, offset), index) =>
            Math.cos(progress.toDouble / (minSineFreqDivider - math.pow(sineFreqDividerDecrease, index)) + offset) * ampl
        }.sum

        val destinationPosition = pos.add(xOffset, progress + 3, zOffset)

        if (destinationPosition.getY > 350)
          world.setBlockState(pos, SkylandsMod.skylands.beanstem.getDefaultState)

        drawLayer(destinationPosition)

        progress += 1

      case _ =>
    }
  }


  private def isReplaceable(world: World, pos: BlockPos): Boolean = {
    val state = world.getBlockState(pos)
    val block = state.getBlock

    state.getBlock.isAir(state, world, pos) ||
      state.getBlock.isLeaves(state, world, pos) ||
      state.getBlock.isWood(world, pos) ||
      Seq(Material.AIR, Material.LEAVES).contains(block.getDefaultState.getMaterial) ||
      Seq(Blocks.GRASS, Blocks.DIRT, Blocks.LOG, Blocks.LOG2, Blocks.SAPLING, Blocks.VINE).contains(block)

  }
}
