package skylands.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import skylands.registry.SkylandsBlocks

// Faithful port of the 1.12.2 BlockArray helper used by BeanstalkGenerator
// and CloudGenerator. A `syncVertical` instance logically joins two dimensions
// along Y: positions with y<=255 go to the `bottom` level, positions above go
// to the `top` level (offset by `overlap - 255` on Y). Writes in the overlap
// band (y in [255-overlap, 255]) hit both levels so block changes stay
// consistent across the seam.
trait BlockArray:
  def getBlockState(position: BlockPos): BlockState
  def setBlockState(position: BlockPos, state: BlockState): Unit

  final def isReplaceable(position: BlockPos): Boolean =
    val s = getBlockState(position)
    s.isAir ||
      s.canBeReplaced ||
      s.is(BlockTags.LEAVES) ||
      s.is(BlockTags.SAPLINGS) ||
      s.is(Blocks.VINE) ||
      s.is(SkylandsBlocks.CLOUD.get())

  final def isTerrainBlock(position: BlockPos): Boolean =
    val s = getBlockState(position)
    s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.STONE)

object BlockArray:
  def forLevel(level: Level): BlockArray = new BlockArray:
    override def getBlockState(p: BlockPos): BlockState = level.getBlockState(p)
    override def setBlockState(p: BlockPos, s: BlockState): Unit =
      level.setBlock(p, s, 3)

  // Matches 1.12.2 BlockArray.syncVertical: the "bottom" level is the overworld,
  // the "top" level is Skylands. Positions y<=255 in bottom coordinate map to
  // (y + overlap - 255) in top. Writes in the overlap band are mirrored.
  def syncVertical(bottom: ServerLevel, top: ServerLevel, overlap: Int): BlockArray =
    val seamY = 255
    val dy = overlap - seamY
    new BlockArray:
      override def getBlockState(p: BlockPos): BlockState =
        if p.getY <= seamY then bottom.getBlockState(p)
        else top.getBlockState(p.offset(0, dy, 0))

      override def setBlockState(p: BlockPos, s: BlockState): Unit =
        if p.getY <= seamY then bottom.setBlock(p, s, 3)
        if p.getY >= seamY - overlap then top.setBlock(p.offset(0, dy, 0), s, 3)
