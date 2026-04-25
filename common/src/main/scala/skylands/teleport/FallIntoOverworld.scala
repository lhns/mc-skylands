package skylands.teleport

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import skylands.registry.SkylandsWorldgen

// Port of 1.12.2 org.lolhens.skylands.feature.FallIntoOverworld.
// If a player in the Skylands dimension drops below y=5, teleport them back
// to the Overworld at the same XZ, landing just under the beanstalk-top
// cloud band so they're back at the seam.
//
// Note: the 1.12.2 class also had a commented-out upward teleport; the
// upward direction (overworld near top, near beanstalk → skylands) lives
// in CloudBlock.entityInside as it did originally.
object FallIntoOverworld:
  private val SkylandsThresholdY: Double = 5.0
  // Match the cloud trigger band used by BeanstalkGenerator + CloudBlock.
  // Land at the *bottom* of the cloud band so the player sits below
  // CloudBlock's teleport gate (which fires only on the upper half) —
  // gives the same 5-block hysteresis 1.12.2 had (gate at 250, landing
  // at 245). Without this, the player teleports back up immediately
  // and loops between dimensions.
  private val OverworldCloudBandHeight: Int = 10

  def onPlayerTick(player: Player): Unit = player match
    case sp: ServerPlayer =>
      val level = sp.level()
      if level.dimension() == SkylandsWorldgen.SKYLANDS_LEVEL && sp.getY <= SkylandsThresholdY then
        val overworld = sp.server.getLevel(Level.OVERWORLD)
        if overworld == null then return
        val seamY = overworld.getMaxBuildHeight - 1
        val landingY = seamY - OverworldCloudBandHeight
        SkylandsTeleport.teleportPlayer(
          sp,
          Level.OVERWORLD,
          new Vec3(sp.getX, landingY.toDouble, sp.getZ)
        )
    case _ => ()
