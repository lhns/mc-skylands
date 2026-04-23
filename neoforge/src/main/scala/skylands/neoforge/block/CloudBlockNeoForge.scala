package skylands.neoforge.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState
import skylands.block.CloudBlock

// NeoForge-only overlay. The `hidesNeighborFace` hook is patched into
// `Block.shouldRenderFace` by NeoForge 21.1 (see patches/net/minecraft/world/
// level/block/Block.java.patch). Vanilla MC — and therefore Fabric — does not
// have it, so this override cannot live in the common CloudBlock.
//
// Called on the NEIGHBOR block when deciding whether to render the face of
// another cloud next to it. `pos` is our (neighbor's) pos, `neighborState` is
// the rendered cloud, `dir` points from us to the rendered cloud.
class CloudBlockNeoForge extends CloudBlock:
  override def hidesNeighborFace(
      level: BlockGetter,
      pos: BlockPos,
      state: BlockState,
      neighborState: BlockState,
      dir: Direction
  ): Boolean =
    val renderedPos = pos.relative(dir)
    val faceDir = dir.getOpposite
    CloudBlock.shouldCullFace(level, renderedPos, faceDir, this)
