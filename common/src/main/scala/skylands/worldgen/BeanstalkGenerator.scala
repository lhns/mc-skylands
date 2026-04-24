package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState
import skylands.block.BeanstalkBlock
import skylands.registry.{SkylandsBlocks, SkylandsWorldgen}

// Deterministic per-layer beanstalk generator.
//
// The stalk is treated as a 1-D curve indexed by a signed layer offset `p`
// from the bean's Y. `stalkCenter(p)` gives the spine position at layer p,
// `thickness(distFromTip)` gives the disk radius a layer of a given age
// should have, and `drawDisk` paints that disk.
//
// Each update advances `progress` by one (new tip), extends the root tip
// by one (new root layer) up to `rootDepth`, and re-sweeps every
// still-existing layer redrawing its disk at the thickness its age demands.
// Thickness grows over time because `distFromTip(p) = progress - |p|`
// increases with progress for any fixed p. Roots sit at negative `p` and
// ride the same sine wobble as the trunk.
//
// State is `{progress, seed}`, both passed in as constructor parameters:
// cold start rolls a fresh `seed` from `level.getRandom`; load constructs
// directly from the persisted NBT. Nothing in this class is mutable-
// reseeded after construction.
class BeanstalkGenerator(
    level: ServerLevel,
    position: BlockPos,
    seed: Long,
    private var progress: Int
) extends StructureGenerator(level, position):

  // Cold-start convenience: fresh seed, zero progress.
  def this(level: ServerLevel, position: BlockPos) =
    this(level, position, level.getRandom.nextLong(), 0)

  // Restore from persisted NBT.
  def this(level: ServerLevel, position: BlockPos, tag: CompoundTag) =
    this(level, position, tag.getLong("seed"), tag.getInt("progress"))

  private val SkylandsOverlap = 15

  // Top of the source dimension's build range. Used for the cloud trigger
  // band, the hard growth cap, and the syncVertical seam when Skylands
  // is available.
  private val seamY: Int = level.getMaxBuildHeight - 1

  // Mirror writes into Skylands when the dimension is available, so the
  // trunk appears continuous across the seam. If Skylands isn't loaded
  // (e.g. dedicated-server config omitted it), fall back to a single-
  // level view — writes above the seam noop via forLevel's bounds check.
  private val syncedWorld: BlockArray =
    val skylands = level.getServer.getLevel(SkylandsWorldgen.SKYLANDS_LEVEL)
    if skylands == null then BlockArray.forLevel(level)
    else BlockArray.syncVertical(level, skylands, SkylandsOverlap, seamY)

  // --- Sine-wobble constants (lifted verbatim from the 1.12.2 port) --------

  private val octaves                 = 7
  private val amplitudeMax            = 40.0
  private val octaveDecrease          = 1.12
  private val minSineFreqDivider      = 30.0
  private val sineFreqDividerDecrease = 0.66

  private val rootDepth       = 5
  private val cloudBandHeight = 10

  // High-frequency sine used for the per-layer jitter. `jitterFreq`
  // controls how much the jitter shifts between adjacent layers (radians
  // per layer). `jitterAmplitude` scales the resulting XZ offset before
  // rounding to an int — small enough that consecutive shell disks still
  // overlap so the trunk stays continuous.
  private val jitterFreq      = 2.0
  private val jitterAmplitude = 1.2

  // All per-beanstalk randomness comes out of this one RandomSource,
  // drawn in a fixed order at construction: 7 main-wobble amplitudes,
  // 7 main-wobble phase offsets, 2 jitter phase offsets. Same `seed` →
  // same sequence → same stalk, always.
  private val (amps, offsets, jitterPhases): (Array[Double], Array[Double], Array[Double]) =
    val r = RandomSource.create(seed)
    val a = Array.tabulate(octaves)(i =>
      (r.nextDouble() - 0.5) * amplitudeMax * 2.0 * (1.0 / math.pow(octaveDecrease, i))
    )
    val o = Array.tabulate(octaves)(i =>
      r.nextDouble() * 2.0 * math.Pi *
        (minSineFreqDivider - math.pow(sineFreqDividerDecrease, i))
    )
    val j = Array(r.nextDouble() * 2.0 * math.Pi, r.nextDouble() * 2.0 * math.Pi)
    (a, o, j)

  // --- Cached block states -------------------------------------------------

  private val beanstalkBlock = SkylandsBlocks.BEANSTALK.get()
  private val centerState: BlockState =
    beanstalkBlock.defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.TRUE)
  private val shellState: BlockState = beanstalkBlock.defaultBlockState()

  // --- Core functions ------------------------------------------------------

  // Signed layer index → world position. p = 0 is the bean's own Y
  // (anchored to `position` exactly — see the -sin(offset_i) / -cos(offset_i)
  // baseline subtractions, which zero the wobble sum at p=0 without
  // flattening the shape elsewhere). p < 0 walks down into the roots,
  // p > 0 climbs the trunk. Per-layer XZ jitter is added on top,
  // deterministically keyed on (seed, p) so reloads land the same positions.
  private def stalkCenter(p: Int): BlockPos =
    var dx = 0.0
    var dz = 0.0
    var i  = 0
    while i < octaves do
      val d = minSineFreqDivider - math.pow(sineFreqDividerDecrease, i)
      dx += (math.sin(p.toDouble / d + offsets(i)) - math.sin(offsets(i))) * amps(i)
      dz += (math.cos(p.toDouble / d + offsets(i)) - math.cos(offsets(i))) * amps(i)
      i += 1
    val (jx, jz) = layerJitter(p)
    position.offset(dx.toInt + jx, p, dz.toInt + jz)

  // High-frequency sine-noise XZ offset per layer — same wobble idiom
  // as `stalkCenter`, just with different amplitude/frequency. Anchored
  // at p=0 via baseline subtraction so no special-case needed for the
  // bean's layer.
  private def layerJitter(p: Int): (Int, Int) =
    val phaseX = jitterPhases(0)
    val phaseZ = jitterPhases(1)
    val jx = (math.sin(p * jitterFreq + phaseX) - math.sin(phaseX)) * jitterAmplitude
    val jz = (math.sin(p * jitterFreq + phaseZ) - math.sin(phaseZ)) * jitterAmplitude
    (jx.round.toInt, jz.round.toInt)

  // Number of ticks between this layer's birth and the current tick.
  // `|p|` works as birth-tick because we extend both tips by one per
  // update, so layer p is born on tick |p|. Freshly-born layers have
  // distFromTip = 0, which yields radius 0 via `thickness`.
  private def distFromTip(p: Int): Int = progress - math.abs(p)

  // Gradual, log-shaped taper. A layer starts as a single CENTER cell
  // (radius 0) and fattens as the tip moves away — matches the 1.12.2
  // feel of `log1p(steps * 1.4)`.
  private def thickness(distFromTip: Int): Int =
    if distFromTip <= 0 then 0
    else math.log1p(distFromTip * 1.4).toInt

  // Beanstalks are allowed to punch through dirt, natural overworld stone,
  // and our own cloud blocks on top of whatever `BlockArray.isReplaceable`
  // already allows. The generic predicate stays generic; this widening is
  // local to this generator.
  private def canOverwrite(pos: BlockPos): Boolean =
    if syncedWorld.isReplaceable(pos) then true
    else
      val s = syncedWorld.getBlockState(pos)
      s.is(BlockTags.DIRT)
        || s.is(BlockTags.BASE_STONE_OVERWORLD)
        || s.is(SkylandsBlocks.CLOUD.get())

  // One idempotent disk. Cells that fail `canOverwrite` are skipped; the
  // next update re-attempts the same layer at the same center, so a
  // transient obstruction is cosmetic rather than structural.
  private def drawDisk(center: BlockPos, radius: Int, state: BlockState): Unit =
    if radius <= 0 then
      if canOverwrite(center) then syncedWorld.setBlockState(center, state)
    else
      val r2 = radius * radius
      var x = -radius
      while x <= radius do
        var z = -radius
        while z <= radius do
          if x * x + z * z <= r2 then
            val p = center.offset(x, 0, z)
            if canOverwrite(p) then syncedWorld.setBlockState(p, state)
          z += 1
        x += 1

  // --- Tick ----------------------------------------------------------------

  override def update(): Unit =
    // Hard cap: tip reached the top of the source dimension. Replace the
    // bean with a plain stem block so the trunk reads as continuous at
    // ground level and the BlockEntity stops ticking (different block =
    // no more serverTick).
    if stalkCenter(progress).getY > seamY then
      level.setBlock(position, shellState, 3)
      return

    progress += 1

    // Both tips grow with progress: trunk goes up to p = progress, root
    // goes down to p = -min(progress, rootDepth). New layers start at
    // distFromTip = 0 (single CENTER cell) and thicken on later ticks.
    // p = 0 is skipped so the bean itself is never boxed in by a shell
    // disk at its own Y — the bean is the visual anchor there.
    val rootFloor = -math.min(progress, rootDepth)
    var p = rootFloor
    while p <= progress do
      if p != 0 then
        val center = stalkCenter(p)
        drawDisk(center, thickness(distFromTip(p)), shellState)
        if canOverwrite(center) then syncedWorld.setBlockState(center, centerState)
      p += 1

    val newTip = stalkCenter(progress)
    if newTip.getY >= seamY - cloudBandHeight && newTip.getY <= seamY then
      new CloudGenerator(level, newTip)

  // --- NBT -----------------------------------------------------------------

  def writeNbt(tag: CompoundTag): Unit =
    tag.putInt("progress", progress)
    tag.putLong("seed", seed)
