package skylands.fabric.client

import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import skylands.block.CloudBlock
import skylands.registry.SkylandsBlocks

import java.util.function.Supplier

// Fabric-side sibling of CloudBlockNeoForge. Vanilla MC (and therefore Fabric)
// does not call `hidesNeighborFace` during mesh building, so we can't match
// 1.12.2's face-culling rule from the Block class. Instead, we wrap the
// vanilla baked model for `skylands:cloud` and filter quads per-face in
// `emitBlockQuads`. The 1.12.2 logic lives in `CloudBlock.shouldCullFace` and
// both platforms call it.
//
// Registered via ModelLoadingPlugin.modifyModelAfterBake in SkylandsFabricClient.
@Environment(EnvType.CLIENT)
class CloudBakedModel(inner: BakedModel) extends ForwardingBakedModel:
  this.wrapped = inner

  // Returning false forces the Fabric Renderer to call emitBlockQuads instead
  // of taking the fast vanilla path — the fast path bypasses QuadTransforms.
  override def isVanillaAdapter: Boolean = false

  override def emitBlockQuads(
      view: BlockAndTintGetter,
      state: BlockState,
      pos: BlockPos,
      randomSupplier: Supplier[RandomSource],
      context: RenderContext
  ): Unit =
    val cloud = SkylandsBlocks.CLOUD.get()
    val transform: RenderContext.QuadTransform = quad =>
      val cull = quad.cullFace()
      cull == null || !CloudBlock.shouldCullFace(view, pos, cull, cloud)
    context.pushTransform(transform)
    try super.emitBlockQuads(view, state, pos, randomSupplier, context)
    finally context.popTransform()
