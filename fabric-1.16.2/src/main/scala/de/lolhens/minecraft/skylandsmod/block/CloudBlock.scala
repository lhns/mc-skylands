package de.lolhens.minecraft.skylandsmod.block

import de.lolhens.minecraft.skylandsmod.SkylandsMod
import de.lolhens.minecraft.skylandsmod.util.ShouldDrawSideContext
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.world.{BlockView, World}

class CloudBlock extends Block(CloudBlock.settings) {
  override def getVisualShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    VoxelShapes.empty

  @Environment(EnvType.CLIENT)
  override def isSideInvisible(state: BlockState, stateFrom: BlockState, direction: Direction): Boolean = {
    if (ShouldDrawSideContext.isActive) {
      val world = ShouldDrawSideContext.world
      val pos = ShouldDrawSideContext.pos

      val vertical: Boolean = direction.getAxis == Axis.Y

      def isCloud(state: BlockState): Boolean =
        state.isOf(this)

      def isHidden(pos: BlockPos): Boolean = {
        val state = world.getBlockState(pos)
        !isCloud(state) && state.isFullCube(world, pos)
      }

      !vertical &&
        isCloud(stateFrom) &&
        !isHidden(pos.offset(direction.rotateYClockwise())) &&
        !isHidden(pos.offset(direction.rotateYClockwise().rotateYClockwise())) &&
        !isHidden(pos.offset(direction.rotateYCounterclockwise()))
    } else
      false
  }

  override def getCollisionShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    VoxelShapes.empty

  override def onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity): Unit = {
    if (entity.getVelocity.y < 0) entity.setVelocity(entity.getVelocity.multiply(1, 0.5, 1)) //entity.motionY = Math.max(entity.motionY, -0.3)
    entity.fallDistance = 0

    val isFlying = entity match {
      case player: PlayerEntity => player.abilities.flying
      case _ => false
    }

    if (!isFlying && entity.getVelocity.y <= 0.1) entity.setOnGround(true)

    def nearCloud(position: BlockPos, radius: Int): Boolean = {
      val mutablePos = position.mutableCopy()
      for {
        x <- -radius to radius
        z <- -radius to radius
      } {
        mutablePos.move(x, 0, z)
        if (world.getBlockState(mutablePos).isOf(SkylandsMod.BEANSTALK_BLOCK)) return true
        mutablePos.move(-x, 0, -z)
      }
      false
    }

    /*entity match {
      case player: EntityPlayerMP if !player.world.isRemote =>
        val playerPos = new BlockPos(player)

        val teleportTarget: Option[(DimensionType, BlockPos)] =
          if (player.dimension == DimensionType.OVERWORLD.getId && position.getY >= 250 && nearCloud(playerPos, 20))
            Some(SkylandsMod.skylands.skylandsDimensionType -> playerPos.add(0, SkylandsMod.skylands.skylandsOverlap - 255, 0))
          else
            None

        for ((dimension, position) <- teleportTarget) {
          val teleporter: Teleporter = new SimpleTeleporter(player.getServer.getWorld(dimension.getId), Some(position))
          player.getServer.getPlayerList.transferPlayerToDimension(player, dimension.getId, teleporter)
        }

      case _ =>
    }*/
  }
}

object CloudBlock {
  val MaterialCloud: Material = new Material.Builder(MaterialColor.WEB) {
    burnable()
  }.build()

  private val settings =
    FabricBlockSettings
      .of(MaterialCloud)
      .nonOpaque()
      .allowsSpawning((_, _, _, _) => false)
      .solidBlock((_, _, _) => false)
      .suffocates((_, _, _) => false)
      .blockVision((_, _, _) => false)
      .hardness(1)
      .resistance(2)
      .sounds(BlockSoundGroup.SNOW)
}
