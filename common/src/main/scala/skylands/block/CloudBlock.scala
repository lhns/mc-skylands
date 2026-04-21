package skylands.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.{Block, SoundType}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}

class CloudBlock extends Block(CloudBlock.Properties):
  override def skipRendering(state: BlockState, adjacent: BlockState, dir: Direction): Boolean =
    adjacent.is(this) || super.skipRendering(state, adjacent, dir)

  override def getVisualShape(state: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape =
    Shapes.empty()

  override def getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape =
    Shapes.empty()

  override def entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity): Unit =
    val velocity = entity.getDeltaMovement
    if velocity.y < 0 then
      entity.setDeltaMovement(velocity.x, velocity.y * 0.5, velocity.z)
    entity.fallDistance = 0f

    val flying = entity match
      case player: Player => player.getAbilities.flying
      case _              => false

    if !flying && entity.getDeltaMovement.y <= 0.1 then
      entity.setOnGround(true)
    // Upward dimension teleport lives in M5.

object CloudBlock:
  private val Properties: BlockBehaviour.Properties =
    BlockBehaviour.Properties.of()
      .mapColor(MapColor.WOOL)
      .strength(1f, 2f)
      .sound(SoundType.SNOW)
      .lightLevel(_ => 2)
      .noOcclusion()
      .noCollission()
      .isSuffocating((_, _, _) => false)
      .isViewBlocking((_, _, _) => false)
      .ignitedByLava()
