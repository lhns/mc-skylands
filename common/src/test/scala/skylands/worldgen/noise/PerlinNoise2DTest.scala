package skylands.worldgen.noise

import net.minecraft.world.level.levelgen.LegacyRandomSource
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals}
import org.junit.jupiter.api.Test

final class PerlinNoise2DTest:

  private def noise(seed: Long, levels: Int): PerlinNoise2D =
    new PerlinNoise2D(new LegacyRandomSource(seed), levels)

  @Test def sameSeedSameOutput(): Unit =
    val a = noise(100L, 8).getValue(12.5, -7.25)
    val b = noise(100L, 8).getValue(12.5, -7.25)
    assertEquals(a, b, 0.0)

  @Test def differentCoordsDifferentOutput(): Unit =
    val n = noise(200L, 8)
    assertNotEquals(n.getValue(0.0, 0.0), n.getValue(50.0, 50.0))

  @Test def differentSeedsDifferentOutput(): Unit =
    assertNotEquals(noise(1L, 8).getValue(3.0, 3.0), noise(2L, 8).getValue(3.0, 3.0))

  @Test def zeroLevelsReturnsZero(): Unit =
    assertEquals(0.0, noise(0L, 0).getValue(1.0, 1.0), 0.0)
