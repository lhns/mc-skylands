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

  override def getVisualShape(state: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape =
    Shapes.empty()

  override def getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape =
    Shapes.empty()

  // Match vanilla AbstractGlassBlock: sunlight passes through clouds, and
  // no ambient-occlusion shading darkens terrain under a cloud layer.
  // `.noOcclusion()` alone isn't always enough — glass sets both anyway.
  override def getLightBlock(state: BlockState, level: BlockGetter, pos: BlockPos): Int = 0

  override def getShadeBrightness(state: BlockState, level: BlockGetter, pos: BlockPos): Float = 1.0f

  override def propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = true

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
    val seamY = level.getMaxBuildHeight - 1
    // Teleport-active zone is the upper half of the cloud band, leaving
    // the lower half as a hysteresis buffer that FallIntoOverworld
    // lands into (so a freshly-fallen player doesn't immediately re-
    // trigger). Same 5/10 split as 1.12.2 (gate at 250, band 245..255).
    if cloudPos.getY < seamY - CloudBandHeight / 2 then return
    val playerPos = player.blockPosition()
    if !isNearBeanstalk(level, playerPos, BeanstalkSearchRadius) then return

    // Mirror math: a write at overworld y maps to Skylands y + (overlap −
    // seamY). Player at the seam top lands at `SkylandsOverlap` (top of
    // the overlap band, i.e. on the trunk's overlap-mirror surface).
    val targetY = playerPos.getY + SkylandsOverlap - seamY
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
  private val BeanstalkSearchRadius: Int = 20
  private val SkylandsOverlap: Int = 15
  // Clouds at `y ∈ [seamY - CloudBandHeight, seamY]` are teleport-capable.
  // Matches the band BeanstalkGenerator uses to spawn cloud clusters.
  private val CloudBandHeight: Int = 10

  // Port of 1.12.2 BlockCloud.shouldSideBeRendered — returns whether the face
  // of the rendered cloud at `selfPos` in direction `faceDir` should be culled
  // (hidden). Loader glue calls this:
  //   - NeoForge: CloudBlockNeoForge.hidesNeighborFace
  //   - Fabric: CloudBakedModel.emitBlockQuads via QuadTransform
  //
  // Logic: cull iff (a) horizontal face AND (b) neighbor in `faceDir` is cloud
  // AND (c) all three horizontal neighbors of the rendered cloud other than
  // `faceDir` are either cloud or air. Any solid non-cloud block on a
  // perpendicular side reveals the interior face — matching how 1.12.2 cloud
  // clusters that touch a beanstalk show their inner wall from inside.
  def shouldCullFace(level: BlockGetter, selfPos: BlockPos, faceDir: Direction, cloudBlock: Block): Boolean =
    if faceDir.getAxis == Direction.Axis.Y then return false
    val neighbor = level.getBlockState(selfPos.relative(faceDir))
    if !neighbor.is(cloudBlock) then return false
    val it = Direction.Plane.HORIZONTAL.iterator
    while it.hasNext do
      val d = it.next()
      if d != faceDir then
        val s = level.getBlockState(selfPos.relative(d))
        if !s.is(cloudBlock) && !s.isAir then return false
    true

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
