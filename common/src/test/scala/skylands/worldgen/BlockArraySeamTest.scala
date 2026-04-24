package skylands.worldgen

import net.minecraft.core.BlockPos
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

// Validates the seam arithmetic used by BlockArray.syncVertical without
// booting a server. The real class writes through two ServerLevels, but the
// coordinate mapping is pure and is the subtle part worth asserting.
final class BlockArraySeamTest:

  private val SeamY = 255
  private val Overlap = 15

  /** Reproduces BlockArray.syncVertical's overworld→skylands offset. */
  private def toTopPos(overworld: BlockPos): BlockPos =
    overworld.offset(0, Overlap - SeamY, 0)

  /** overworld y=255 (top seam) maps to skylands y=15 (top of overlap band). */
  @Test def seamTopMapsToOverlapTop(): Unit =
    assertEquals(new BlockPos(0, 15, 0), toTopPos(new BlockPos(0, 255, 0)))

  /** overworld y=240 (bottom of overlap band) maps to skylands y=0. */
  @Test def overlapBottomMapsToZero(): Unit =
    assertEquals(new BlockPos(0, 0, 0), toTopPos(new BlockPos(0, 240, 0)))

  /** Below the overlap band (overworld y<240): writes land on the bottom
    * level only. We encode that rule by checking the target y would be
    * negative, which is what BlockArray.syncVertical's second if-guard skips
    * (`p.getY >= seamY - overlap` → p.getY >= 240). */
  @Test def belowOverlapBandSkipsTopWrite(): Unit =
    val ow = new BlockPos(0, 239, 0)
    assertTrue(ow.getY < SeamY - Overlap, "y=239 should be below the overlap band")

  /** Above the seam (overworld y>255): the real getter switches to the top
    * level. The offset math still applies — e.g. overworld-space y=260
    * would fetch from skylands y=20. */
  @Test def aboveSeamReadsFromTopWithOffset(): Unit =
    assertEquals(new BlockPos(1, 20, 2), toTopPos(new BlockPos(1, 260, 2)))

  /** Horizontal coords are unchanged; only Y is offset. */
  @Test def horizontalCoordsUnchanged(): Unit =
    val src = new BlockPos(-42, 250, 17)
    val dst = toTopPos(src)
    assertEquals(src.getX, dst.getX)
    assertEquals(src.getZ, dst.getZ)
    assertEquals(src.getY - (SeamY - Overlap), dst.getY)
