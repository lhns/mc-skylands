package skylands.gameplay

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import skylands.registry.SkylandsWorldgen

// Faithful port of 1.12.2 org.lolhens.skylands.feature.FeatherGliding.
// When a player in Skylands holds a feather in either hand (and isn't in
// creative flight), falling is capped at -0.3 y/tick and fall distance is
// reset — gliding instead of plummeting.
//
// 1.12.2 also set `player.jumpMovementFactor = 0.1f` to increase horizontal
// air control. That field is encapsulated on 1.21 LivingEntity with no
// public setter, so the matching behavior would need a mixin. We preserve
// the fall-speed cap and fall-distance reset — the core gliding feel — and
// leave horizontal agility to vanilla airborne control.
object FeatherGliding:
  private val MaxFallSpeed: Double = -0.3

  def onPlayerTick(player: Player): Unit =
    if player.level().dimension() != SkylandsWorldgen.SKYLANDS_LEVEL then return
    if player.getAbilities.flying then return

    val holdingFeather =
      player.getItemInHand(InteractionHand.MAIN_HAND).`is`(Items.FEATHER) ||
        player.getItemInHand(InteractionHand.OFF_HAND).`is`(Items.FEATHER)
    if !holdingFeather then return

    val v = player.getDeltaMovement
    if v.y <= MaxFallSpeed then
      player.setDeltaMovement(v.x, MaxFallSpeed, v.z)
      player.fallDistance = 0f
