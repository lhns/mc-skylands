package skylands.teleport

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import skylands.registry.SkylandsWorldgen

// Faithful port of 1.12.2 org.lolhens.skylands.feature.FallIntoOverworld.
// If a player in the Skylands dimension drops below y=5, teleport them back
// to the Overworld at the same XZ at y=245 (just under the beanstalk-top
// cloud layer).
//
// Note: the 1.12.2 class also had a commented-out upward teleport; the
// upward direction (overworld y>=250 near beanstalk -> skylands) lives
// in CloudBlock.entityInside as it did originally.
object FallIntoOverworld:
  private val SkylandsThresholdY: Double = 5.0
  private val OverworldLandingY: Double = 245.0

  def onPlayerTick(player: Player): Unit = player match
    case sp: ServerPlayer =>
      val level = sp.level()
      if level.dimension() == SkylandsWorldgen.SKYLANDS_LEVEL && sp.getY <= SkylandsThresholdY then
        SkylandsTeleport.teleportPlayer(
          sp,
          Level.OVERWORLD,
          new Vec3(sp.getX, OverworldLandingY, sp.getZ)
        )
    case _ => ()
