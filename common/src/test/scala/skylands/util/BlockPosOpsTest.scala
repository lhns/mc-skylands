package skylands.util

import net.minecraft.core.BlockPos
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test

final class BlockPosOpsTest:

  @Test def originIsInsideAnyPositiveRadius(): Unit =
    assertTrue(new BlockPos(0, 0, 0).inSphere(0.1))

  @Test def radiusIsStrict(): Unit =
    // inSphere uses `<` (not `<=`), so a point exactly at radius is outside.
    assertFalse(new BlockPos(3, 4, 0).inSphere(5.0))

  @Test def justInsideIsInside(): Unit =
    assertTrue(new BlockPos(3, 4, 0).inSphere(5.0001))

  @Test def threeDMagnitude(): Unit =
    // Classic 3-4-12 triple: 9 + 16 + 144 = 169 = 13^2.
    assertFalse(new BlockPos(3, 4, 12).inSphere(13.0))
    assertTrue(new BlockPos(3, 4, 12).inSphere(13.5))

  @Test def negativeCoordinatesWork(): Unit =
    assertTrue(new BlockPos(-3, -4, 0).inSphere(6.0))
    assertFalse(new BlockPos(-5, -5, -5).inSphere(8.0))
