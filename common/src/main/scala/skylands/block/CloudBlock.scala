package skylands.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.{ServerLevel, ServerPlayer}
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.{Block, SoundType}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}
import skylands.registry.{SkylandsBlocks, SkylandsWorldgen}
import skylands.teleport.SkylandsTeleport

class CloudBlock extends Block(CloudBlock.Properties):
  import CloudBlock._

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

    (entity, level) match
      case (player: ServerPlayer, sl: ServerLevel) =>
        tryTeleportToSkylands(player, sl, pos)
      case _ => ()

  private def tryTeleportToSkylands(player: ServerPlayer, level: ServerLevel, cloudPos: BlockPos): Unit =
    if level.dimension() != Level.OVERWORLD then return
    if cloudPos.getY < TeleportMinY then return
    val playerPos = player.blockPosition()
    if !isNearBeanstalk(level, playerPos, BeanstalkSearchRadius) then return

    // 1.12.2 offset: playerPos.add(0, skylandsOverlap - 255, 0) — y=250 lands at y=10 in Skylands.
    val targetY = playerPos.getY + SkylandsOverlap - 255
    SkylandsTeleport.teleportPlayer(
      player,
      SkylandsWorldgen.SKYLANDS_LEVEL,
      new Vec3(playerPos.getX + 0.5, targetY.toDouble, playerPos.getZ + 0.5)
    )

  private def isNearBeanstalk(level: ServerLevel, center: BlockPos, radius: Int): Boolean =
    val beanstalk = SkylandsBlocks.BEANSTALK.get()
    val probe = new BlockPos.MutableBlockPos()
    var dx = -radius
    while dx <= radius do
      var dz = -radius
      while dz <= radius do
        probe.set(center.getX + dx, center.getY, center.getZ + dz)
        if level.getBlockState(probe).is(beanstalk) then return true
        dz += 1
      dx += 1
    false

object CloudBlock:
  private val TeleportMinY: Int = 250
  private val BeanstalkSearchRadius: Int = 20
  private val SkylandsOverlap: Int = 15

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
