package skylands.teleport

import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.{ServerLevel, ServerPlayer}
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.DimensionTransition
import net.minecraft.world.phys.Vec3

// Cross-platform teleport between dimensions. In 1.21.1 the vanilla
// Mojmap API Entity#changeDimension(DimensionTransition) is already
// present on both Fabric and NeoForge, so no @ExpectPlatform split
// is necessary.
object SkylandsTeleport:
  def teleportPlayer(
      player: ServerPlayer,
      target: ResourceKey[Level],
      pos: Vec3
  ): Unit =
    val server = player.server
    val targetLevel: ServerLevel = server.getLevel(target)
    if targetLevel == null then return

    val transition = new DimensionTransition(
      targetLevel,
      pos,
      Vec3.ZERO,
      player.getYRot,
      player.getXRot,
      DimensionTransition.DO_NOTHING
    )
    player.changeDimension(transition)
