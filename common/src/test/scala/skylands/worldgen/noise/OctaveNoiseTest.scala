package skylands.worldgen.noise

import net.minecraft.world.level.levelgen.LegacyRandomSource
import org.junit.jupiter.api.Assertions.{assertArrayEquals, assertEquals, assertNotEquals, assertTrue}
import org.junit.jupiter.api.Test

final class OctaveNoiseTest:

  private def noise(seed: Long, octaves: Int): OctaveNoise =
    new OctaveNoise(new LegacyRandomSource(seed), octaves)

  @Test def sameSeedSameOutput(): Unit =
    val a = noise(42L, 4).generateNoiseOctaves(null, 0, 0, 0, 3, 33, 3, 1.0, 1.0, 1.0)
    val b = noise(42L, 4).generateNoiseOctaves(null, 0, 0, 0, 3, 33, 3, 1.0, 1.0, 1.0)
    assertArrayEquals(a, b, 0.0)

  @Test def differentSeedsDifferentOutput(): Unit =
    val a = noise(1L, 4).generateNoiseOctaves(null, 0, 0, 0, 3, 33, 3, 1.0, 1.0, 1.0)
    val b = noise(2L, 4).generateNoiseOctaves(null, 0, 0, 0, 3, 33, 3, 1.0, 1.0, 1.0)
    // At least one cell should differ — seeds produce different permutation tables
    // and xo/yo/zo offsets.
    var anyDiff = false
    var i = 0
    while i < a.length do
      if a(i) != b(i) then anyDiff = true
      i += 1
    assertTrue(anyDiff, "different seeds should produce different noise")

  @Test def passingTargetArrayReusesIt(): Unit =
    val target = new Array[Double](3 * 33 * 3)
    // Pre-fill with garbage the impl must clear.
    java.util.Arrays.fill(target, 999.0)
    val out = noise(7L, 4).generateNoiseOctaves(target, 0, 0, 0, 3, 33, 3, 1.0, 1.0, 1.0)
    assertTrue(out eq target, "target array should be reused, not reallocated")
    // After the call, no cell should still hold the sentinel.
    var stillSentinel = false
    var i = 0
    while i < out.length do
      if out(i) == 999.0 then stillSentinel = true
      i += 1
    assertEquals(false, stillSentinel)

  @Test def offsetShiftsSample(): Unit =
    // Sampling at different xOffset should give a different value set.
    val a = noise(99L, 4).generateNoiseOctaves(null, 0, 0, 0, 1, 1, 1, 1.0, 1.0, 1.0)
    val b = noise(99L, 4).generateNoiseOctaves(null, 1000, 0, 0, 1, 1, 1, 1.0, 1.0, 1.0)
    assertNotEquals(a(0), b(0))

  @Test def zeroOctavesProducesZeros(): Unit =
    val out = noise(0L, 0).generateNoiseOctaves(null, 0, 0, 0, 2, 2, 2, 1.0, 1.0, 1.0)
    out.foreach(v => assertEquals(0.0, v, 0.0))
