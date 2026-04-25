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
// The stalk's XZ offset at layer p is the sum of two pieces:
//   sineWobble(p)   — a smooth 7-octave sine curve evaluated directly
//                     (position-space, anchored at p=0). Sets the big
//                     S-shape the trunk follows.
//   Σ_{k=1..p} jitterStep(k)  — cumulative per-layer ±1/0 jitter from a
//                     high-frequency noise. Bounded drunken-walk that
//                     roughens the trunk's surface on top of the smooth
//                     shape. Matches the 1.12.2 feel of per-tick unit-
//                     step perturbations.
//
// The sine wobble is evaluated once per layer (not derivative-then-
// reintegrated); only the jitter needs accumulating. `update()`
// advances `progress` by one, then re-sweeps every layer from
// `-rootDepth` up to `progress`, keeping a running jitter total so the
// whole sweep is O(progress). Thickness grows over time because
// `distFromTip(p) = progress - |p|` increases for any fixed p, so the
// same layer's disk fattens each tick. Roots sit at negative `p` and
// use a separate jitter accumulator going downward.
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

  // The trunk keeps extending past `seamY` by this many blocks. Those
  // writes fall out of overworld build-range (they noop on the bottom
  // level via BlockArray.forLevel's bounds check) and only land in
  // Skylands via `syncVertical`'s offset `dy = overlap − seamY`. Result:
  // a ladder inside Skylands from the overlap mirror (skylands y=0..15)
  // all the way up to near the island terrain top, so a player that
  // teleported in via a seam-band cloud has something solid to climb
  // instead of free-falling through void.
  //
  // Sized to land the skylands-side trunk top just below
  // `SkylandsChunkGenerator.TerrainTopY`:
  //   skylands-Y top  = overworld-Y cap − (seamY − overlap)
  //   TerrainTopY − 1 = seamY + overshoot − seamY + overlap
  //   overshoot       = TerrainTopY − 1 − overlap
  private val trunkOvershootIntoSkylands: Int =
    SkylandsChunkGenerator.TerrainTopY - 1 - SkylandsOverlap

  private val hardCapY: Int = seamY + trunkOvershootIntoSkylands

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
  private val amplitudeMax            = 40.0    // walker target amplitude; see walkStep — walker enforces ±1/layer regardless
  private val octaveDecrease          = 1.12
  private val minSineFreqDivider      = 30.0
  private val sineFreqDividerDecrease = 0.66

  private val rootDepth       = 5
  private val cloudBandHeight = 10

  // High-frequency sine used as continuous direction perturbation in
  // walkStep. `jitterAmplitude` is the magnitude per axis; tuned to be
  // comparable to a typical `target − current` distance (~5..20 in
  // practice with `amplitudeMax = 40`). Big enough that jitter flips
  // step signs occasionally even when walker isn't right at the target,
  // matching 1.12.2's `direction + rand(±15)` perturbation. Walker
  // still emits ±1/0 per axis, so the connectivity guarantee holds.
  private val jitterFreq      = 2.0
  private val jitterAmplitude = 10.0

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

  // Smooth 7-octave sine curve, evaluated directly as the walker's
  // target offset at layer p. Anchored at p=0 by subtracting each
  // octave's value at zero — `sineWobble(0) == (0, 0)` so the stalk's
  // spine passes through the bean. Walker chases this; amplitude sets
  // how far the walker drifts, not how much it moves per layer.
  private def sineWobble(p: Int): (Int, Int) =
    var dx = 0.0
    var dz = 0.0
    var i  = 0
    while i < octaves do
      val d   = minSineFreqDivider - math.pow(sineFreqDividerDecrease, i)
      val arg = p.toDouble / d + offsets(i)
      dx += (math.sin(arg) - math.sin(offsets(i))) * amps(i)
      dz += (math.cos(arg) - math.cos(offsets(i))) * amps(i)
      i += 1
    (dx.toInt, dz.toInt)

  // Continuous per-layer direction perturbation. Two independent
  // high-frequency sines (one phase per axis), scaled to
  // `jitterAmplitude`. NOT sign-quantised here — `walkStep` adds this
  // to `target − current` before signing, so jitter magnitude can flip
  // the step sign near the target and break up monotonic diagonal
  // stretches. Same role as 1.12.2's `direction + rand(±15)`.
  private def jitterStep(p: Int): (Double, Double) =
    (math.sin(p * jitterFreq + jitterPhases(0)) * jitterAmplitude,
     math.sin(p * jitterFreq + jitterPhases(1)) * jitterAmplitude)

  // One step of the walker. Sums target direction + continuous jitter,
  // sign-quantises each axis to ±1/0. `sign(target − current + jitter)`
  // matches 1.12.2 `direction = destination − lastBlockPos; step =
  // round(normalize(direction + jitter))`. Output is in {−1, 0, +1}²
  // per axis — consecutive CENTERs always edge- or face-adjacent.
  private def walkStep(p: Int, cx: Int, cz: Int): (Int, Int) =
    val (tx, tz) = sineWobble(p)
    val (jx, jz) = jitterStep(p)
    (math.signum((tx - cx).toDouble + jx).toInt,
     math.signum((tz - cz).toDouble + jz).toInt)

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

  // One idempotent disk. Center cell gets `core`, the rest gets `shell`.
  // Every write is `canOverwrite`-gated, so cells we don't own (bean,
  // player builds, existing stem) are preserved — a spine running through
  // someone's house doesn't chew through it. Transient obstructions are
  // cosmetic rather than structural because the next update re-attempts
  // the same layer at the same center.
  private def drawDisk(center: BlockPos, radius: Int, shell: BlockState, core: BlockState): Unit =
    if canOverwrite(center) then syncedWorld.setBlockState(center, core)
    if radius > 0 then
      val r2 = radius * radius
      var x = -radius
      while x <= radius do
        var z = -radius
        while z <= radius do
          if (x != 0 || z != 0) && x * x + z * z <= r2 then
            val p = center.offset(x, 0, z)
            if canOverwrite(p) then syncedWorld.setBlockState(p, shell)
          z += 1
        x += 1

  // --- Tick ----------------------------------------------------------------

  override def update(): Unit =
    // Hard cap. Y is deterministic (tip at position.y + progress). The
    // cap sits above `seamY` so the trunk keeps extending into Skylands
    // via syncVertical — see `trunkOvershootIntoSkylands` above.
    //
    // When the cap trips, replace the bean with a stem block so the
    // trunk reads continuous at ground level and the BlockEntity stops
    // ticking (different block = no more serverTick).
    if position.getY + progress > hardCapY then
      level.setBlock(position, centerState, 3)
      return

    progress += 1
    val rootFloor = -math.min(progress, rootDepth)

    // Trunk: unit-step walker chasing sineWobble. Starts at p=0 with
    // (cx, cz) = (0, 0) — first iteration draws at `position` exactly
    // (the bean's layer; drawDisk's canOverwrite protects the bean).
    // Draw then step: consecutive CENTERs are always ±1/0 per axis
    // apart (edge- or face-adjacent).
    var cx = 0
    var cz = 0
    var p  = 0
    while p <= progress do
      drawDisk(position.offset(cx, p, cz), thickness(distFromTip(p)), shellState, centerState)
      p += 1
      if p <= progress then
        val (dx, dz) = walkStep(p, cx, cz)
        cx += dx; cz += dz
    val trunkTipX = cx
    val trunkTipZ = cz

    // Roots: independent walker going down, chasing the same sineWobble
    // curve on the negative side. Starts at p=-1 (p=0 is the bean,
    // already drawn by the trunk loop).
    cx = 0; cz = 0
    p = -1
    while p >= rootFloor do
      val (dx, dz) = walkStep(p, cx, cz)
      cx += dx; cz += dz
      drawDisk(position.offset(cx, p, cz), thickness(distFromTip(p)), shellState, centerState)
      p -= 1

    val newTip = position.offset(trunkTipX, progress, trunkTipZ)
    if newTip.getY >= seamY - cloudBandHeight && newTip.getY <= seamY then
      new CloudGenerator(level, newTip)

  // --- NBT -----------------------------------------------------------------

  def writeNbt(tag: CompoundTag): Unit =
    tag.putInt("progress", progress)
    tag.putLong("seed", seed)
