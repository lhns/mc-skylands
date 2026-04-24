package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
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

  private val MaxRootDepth: Int = 5

  private var progress: Int = 0
  private var lastBlockPos: BlockPos = position

  private lazy val beanBlock      = SkylandsBlocks.BEAN.get()
  private lazy val beanstalkBlock = SkylandsBlocks.BEANSTALK.get()
  private lazy val centerState: BlockState =
    beanstalkBlock.defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.TRUE)
  private lazy val shellState: BlockState = beanstalkBlock.defaultBlockState()

  // The trunk thickens with distance from the growing tip: step 0 is the tip,
  // step N is N center-blocks away. Same formula for both trunk recursion
  // and manual root passes.
  private def shellSizeAt(steps: Int): Int = math.log1p(steps * 1.4).toInt

  private def drawBlock(pos: BlockPos, state: BlockState): Unit =
    if syncedWorld.isReplaceable(pos) || syncedWorld.isTerrainBlock(pos) then
      syncedWorld.setBlockState(pos, state)

  // Shell draw with a relaxed replacement rule at the base of the stalk.
  // Everything at y <= bean.y + 1 gets punched through (walls, planks,
  // deepslate, …) so the dirt encasement and whatever the player built
  // around it doesn't leave a ring of uncarved material hiding the trunk.
  // Above that level we fall back to the normal terrain-only rule. The bean
  // itself and existing beanstalk blocks are always preserved.
  private def drawShellBlock(pos: BlockPos): Unit =
    if pos.getY <= position.getY + 1 then
      val existing = syncedWorld.getBlockState(pos)
      if !existing.is(beanBlock) && !existing.is(beanstalkBlock) then
        syncedWorld.setBlockState(pos, shellState)
    else drawBlock(pos, shellState)

  // One horizontal disk of shell blocks around `center`, sized per the trunk
  // thickening formula. Called by both the trunk's recursion and the root
  // grower.
  private def drawShellDisk(center: BlockPos, steps: Int): Unit =
    val size = shellSizeAt(steps)
    var x = -size
    while x <= size do
      var z = -size
      while z <= size do
        if new BlockPos(x, 0, z).inSphere(size + 0.1) then
          drawShellBlock(center.offset(x, 0, z))
        z += 1
      x += 1

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

    drawBlock(lastBlockPos, centerState)

    def recursiveFunction(currentPos: BlockPos, steps: Int): Unit =
      if steps > 600 then return

      drawShellDisk(currentPos, steps)

      if currentPos.getY >= position.getY then
        var dx = -1
        while dx <= 1 do
          var dz = -1
          while dz <= 1 do
            val newPos = currentPos.offset(dx, -1, dz)
            val bs = syncedWorld.getBlockState(newPos)
            if bs.is(beanstalkBlock)
              && bs.getValue(BeanstalkBlock.CENTER).booleanValue()
              && !newPos.equals(currentPos)
            then recursiveFunction(newPos, steps + 1)
            dz += 1
          dx += 1
    end recursiveFunction

    recursiveFunction(lastBlockPos, 0)

    if lastBlockPos.getY >= 245 && lastBlockPos.getY <= 255 then
      new CloudGenerator(level, lastBlockPos)

  // Grow the trunk down into the ground up to MaxRootDepth blocks. The bean
  // is only allowed to sprout when fully encased in dirt, so rooting is how
  // the trunk breaks out the underside of that dirt pocket.
  //
  // Runs every update (not just once) for two reasons:
  //   1. The main trunk's recursive shell pass can't cross the bean block
  //      (the bean is not a beanstalk CENTER so recursion dead-ends at
  //      `position`). So the roots never get thickened by the normal trunk
  //      logic — we have to thicken them here.
  //   2. Shell size is derived from `progress`, matching how the trunk
  //      gradually fattens up at its base as it grows taller.
  //
  // Stops at the bean or anything truly solid (stone, cobble, …). A root
  // position that already holds beanstalk is left in place (still gets its
  // shell redrawn); a dirt or replaceable block gets overwritten with a
  // CENTER beanstalk.
  private def growRoots(): Unit =
    // dy=0 is a shell-only pass at the bean's own y: the trunk shells only
    // reach down to bean.y+1 and the root shells only up to bean.y-1, so
    // without this the bean sits in an untouched ring of whatever. The
    // shell helper preserves the bean itself, so it's safe unconditionally.
    var dy = 0
    while dy <= MaxRootDepth do
      val rootPos = position.below(dy)

      if dy > 0 then
        val existing = syncedWorld.getBlockState(rootPos)
        if existing.is(beanBlock) then return
        if !existing.is(beanstalkBlock) then
          if !(existing.is(BlockTags.DIRT) || syncedWorld.isReplaceable(rootPos)) then return
          syncedWorld.setBlockState(rootPos, centerState)

      drawShellDisk(rootPos, progress + math.max(dy, 1))

      dy += 1

  override def update(): Unit =
    growRoots()

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
      level.setBlock(position, shellState, 3)

    drawLayer(destinationPosition)

    progress += 1
