package skylands.gameplay

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import skylands.registry.SkylandsWorldgen

// Faithful port of 1.12.2 org.lolhens.skylands.feature.FeatherGliding.
//
// When a player in Skylands holds a feather (either hand, not in creative
// flight) and is falling faster than -0.3 y/tick:
//   - vertical velocity is clamped to -0.3
//   - fall distance is reset
//   - horizontal air control is boosted the same way 1.12.2 did via
//     `jumpMovementFactor = 0.1f`
//
// 1.12.2 set jumpMovementFactor on the player, which the vanilla movement
// code then multiplied into each tick's WASD-derived horizontal impulse,
// bumping it from the default airborne ~0.02 to 0.1. In 1.21 that field is
// encapsulated on LivingEntity with no public setter, but the *effect* is
// equivalent to applying an extra ~0.08 horizontal acceleration per tick in
// the direction of the player's input. The player's raw input is exposed on
// LivingEntity as `xxa` (strafe, -1..1) and `zza` (forward, -1..1), which
// ServerGamePacketListenerImpl populates from movement packets server-side,
// so we can read and act on them without a mixin.
object FeatherGliding:
  private val MaxFallSpeed: Double = -0.3
  // Extra horizontal acceleration per tick = 1.12.2 jumpMovementFactor(0.1)
  // minus the default airborne factor (~0.02).
  private val ExtraAirControl: Double = 0.08

  def onPlayerTick(player: Player): Unit =
    if player.level().dimension() != SkylandsWorldgen.SKYLANDS_LEVEL then return
    if player.getAbilities.flying then return
    if player.onGround() then return

    val holdingFeather =
      player.getItemInHand(InteractionHand.MAIN_HAND).`is`(Items.FEATHER) ||
        player.getItemInHand(InteractionHand.OFF_HAND).`is`(Items.FEATHER)
    if !holdingFeather then return

    val v = player.getDeltaMovement

    if v.y <= MaxFallSpeed then
      player.setDeltaMovement(v.x, MaxFallSpeed, v.z)
      player.fallDistance = 0f
      boostAirControl(player)

  private def boostAirControl(player: Player): Unit =
    val strafe = player.xxa.toDouble
    val forward = player.zza.toDouble
    val magSq = strafe * strafe + forward * forward
    if magSq < 1e-4 then return

    // Match moveRelative's diagonal normalization so W+A isn't 1.4x W alone.
    val mag = Math.sqrt(magSq)
    val scale = ExtraAirControl / Math.max(mag, 1.0)

    val yawRad = Math.toRadians(player.getYRot.toDouble)
    val sin = Math.sin(yawRad)
    val cos = Math.cos(yawRad)

    val dx = (strafe * cos - forward * sin) * scale
    val dz = (forward * cos + strafe * sin) * scale

    val v = player.getDeltaMovement
    player.setDeltaMovement(v.x + dx, v.y, v.z + dz)
    player.hasImpulse = true
