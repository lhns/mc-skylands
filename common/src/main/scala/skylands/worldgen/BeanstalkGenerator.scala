package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import skylands.block.BeanstalkBlock
import skylands.registry.{SkylandsBlocks, SkylandsWorldgen}
import skylands.util.*

// Faithful port of 1.12.2 org.lolhens.skylands.generator.BeanstalkGenerator.
//
// Grows a wavy, sine-wave-offset beanstalk structure upward from the planted
// bean, syncing blocks across the Overworld/Skylands seam through
// BlockArray.syncVertical. The same octave amplitudes, frequency divider
// decrease, random offset ranges, and recursive branch placement as the
// original are preserved.
class BeanstalkGenerator(level: ServerLevel, position: BlockPos) extends StructureGenerator(level, position):
  private val SkylandsOverlap = 15

  private val syncedWorld: BlockArray = {
    val skylands = level.getServer.getLevel(SkylandsWorldgen.SKYLANDS_LEVEL)
    if skylands == null then BlockArray.forLevel(level)
    else BlockArray.syncVertical(level, skylands, SkylandsOverlap)
  }

  private val perlinNoiseSLOctaves = 7
  private val amplitudeMax = 40.0
  private val octaveDecrease = 1.12
  private val minSineFreqDivider = 30.0
  private val sineFreqDividerDecrease = 0.66
  private val maxSingleOffsetX = 30
  private val maxSingleOffsetZ = 30

  // The original uses Scala's global util.Random for per-beanstalk unique offsets;
  // keep the same non-determinism so each planted bean draws a fresh wobble shape.
  private val sineLayerAmplitudes: IndexedSeq[Double] =
    IndexedSeq.tabulate(perlinNoiseSLOctaves)(i =>
      (scala.util.Random.nextDouble() - 0.5) * amplitudeMax * 2.0 * (1.0 / math.pow(octaveDecrease, i))
    )
  private val sineLayerOffset: IndexedSeq[Double] =
    IndexedSeq.tabulate(perlinNoiseSLOctaves)(i =>
      scala.util.Random.nextDouble() * 2.0 * math.Pi * (minSineFreqDivider - math.pow(sineFreqDividerDecrease, i))
    )

  private var progress: Int = 0
  private var lastBlockPos: BlockPos = position

  private def drawBlock(pos: BlockPos, state: BlockState): Unit =
    if syncedWorld.isReplaceable(pos) || syncedWorld.isTerrainBlock(pos) then
      syncedWorld.setBlockState(pos, state)

  private def drawLayer(destinationPosition: BlockPos): Unit =
    val dir = destinationPosition.subtract(lastBlockPos)
    val rand = level.getRandom
    val rdDir = dir.offset(
      rand.nextInt(maxSingleOffsetX) - (maxSingleOffsetX / 2),
      0,
      rand.nextInt(maxSingleOffsetZ) - (maxSingleOffsetZ / 2)
    )

    val rdLen =
      val sqMax = math.max(math.max(rdDir.getX * rdDir.getX, rdDir.getY * rdDir.getY), rdDir.getZ * rdDir.getZ)
      math.sqrt(sqMax.toDouble)
    val step = new BlockPos(
      if rdLen == 0.0 then 0 else math.round(rdDir.getX / rdLen).toInt,
      1,
      if rdLen == 0.0 then 0 else math.round(rdDir.getZ / rdLen).toInt
    )

    lastBlockPos = lastBlockPos.offset(step)

    val centerState: BlockState =
      SkylandsBlocks.BEANSTALK.get().defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.TRUE)
    val shellState: BlockState = SkylandsBlocks.BEANSTALK.get().defaultBlockState()

    drawBlock(lastBlockPos, centerState)

    def recursiveFunction(currentPos: BlockPos, steps: Int): Unit =
      if steps > 600 then return
      val size = math.log1p(steps * 1.4).toInt

      var x = -size
      while x <= size do
        var z = -size
        while z <= size do
          if new BlockPos(x, 0, z).inSphere(size + 0.1) then
            drawBlock(currentPos.offset(x, 0, z), shellState)
          z += 1
        x += 1

      if currentPos.getY >= position.getY then
        var dx = -1
        while dx <= 1 do
          var dz = -1
          while dz <= 1 do
            val newPos = currentPos.offset(dx, -1, dz)
            val bs = syncedWorld.getBlockState(newPos)
            if bs.is(SkylandsBlocks.BEANSTALK.get())
              && bs.getValue(BeanstalkBlock.CENTER).booleanValue()
              && !newPos.equals(currentPos)
            then recursiveFunction(newPos, steps + 1)
            dz += 1
          dx += 1
    end recursiveFunction

    recursiveFunction(lastBlockPos, 0)

    if lastBlockPos.getY >= 245 && lastBlockPos.getY <= 255 then
      new CloudGenerator(level, lastBlockPos)

  override def update(): Unit =
    var xOffset = 0.0
    var zOffset = 0.0
    var i = 0
    while i < perlinNoiseSLOctaves do
      val amp = sineLayerAmplitudes(i)
      val offset = sineLayerOffset(i)
      val divider = minSineFreqDivider - math.pow(sineFreqDividerDecrease, i)
      xOffset += math.sin(progress.toDouble / divider + offset) * amp
      zOffset += math.cos(progress.toDouble / divider + offset) * amp
      i += 1

    val destinationPosition = position.offset(xOffset.toInt, progress + 3, zOffset.toInt)

    if destinationPosition.getY > 430 then
      level.setBlock(position, SkylandsBlocks.BEANSTALK.get().defaultBlockState(), 3)

    drawLayer(destinationPosition)

    progress += 1
