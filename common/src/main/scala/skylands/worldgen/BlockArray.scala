package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

// Two-level seam helper used by BeanstalkGenerator and CloudGenerator. A
// `syncVertical` instance logically joins two dimensions along Y: positions
// with y <= seamY go to the `bottom` level, positions above go to `top`
// (with a Y offset so the top's origin lines up with the seam). Writes in
// the overlap band (y ∈ [seamY - overlap, seamY]) hit both levels so block
// changes stay consistent across the join.
trait BlockArray:
  def getBlockState(position: BlockPos): BlockState
  def setBlockState(position: BlockPos, state: BlockState): Unit

  // Generic "is this block free to paint over?" predicate — vanilla terms
  // only. Air, engine-marked replaceable (tall grass, snow, water, lava,
  // ...), leaves/saplings/vines. Per-generator rules that want a wider
  // set (e.g. beanstalks also replacing dirt, stone, clouds) add their
  // own predicate on top and do not modify this one.
  final def isReplaceable(position: BlockPos): Boolean =
    val s = getBlockState(position)
    s.isAir
      || s.canBeReplaced
      || s.is(BlockTags.LEAVES)
      || s.is(BlockTags.SAPLINGS)
      || s.is(Blocks.VINE)

object BlockArray:
  // Level-adapter view. Positions outside the level's build-height range
  // (y < minY or y >= maxBuildHeight) are clamped: reads return VOID_AIR
  // and writes silently noop. Vanilla Level already behaves this way, but
  // making the clamp explicit here means generators can sweep past the
  // top without needing a separate range check at every call site.
  def forLevel(level: Level): BlockArray = new BlockArray:
    override def getBlockState(p: BlockPos): BlockState =
      if level.isOutsideBuildHeight(p.getY) then Blocks.VOID_AIR.defaultBlockState()
      else level.getBlockState(p)
    override def setBlockState(p: BlockPos, s: BlockState): Unit =
      if !level.isOutsideBuildHeight(p.getY) then level.setBlock(p, s, 3)

  // Join `bottom` (source, e.g. Overworld) and `top` (Skylands) along Y at
  // `seamY`. Positions y<=seamY map to bottom at p, y>seamY map to top at
  // (p + overlap - seamY). Writes in the overlap band mirror to both.
  def syncVertical(bottom: ServerLevel, top: ServerLevel, overlap: Int, seamY: Int): BlockArray =
    val dy = overlap - seamY
    new BlockArray:
      override def getBlockState(p: BlockPos): BlockState =
        if p.getY <= seamY then bottom.getBlockState(p)
        else top.getBlockState(p.offset(0, dy, 0))

      override def setBlockState(p: BlockPos, s: BlockState): Unit =
        if p.getY <= seamY then bottom.setBlock(p, s, 3)
        if p.getY >= seamY - overlap then top.setBlock(p.offset(0, dy, 0), s, 3)
