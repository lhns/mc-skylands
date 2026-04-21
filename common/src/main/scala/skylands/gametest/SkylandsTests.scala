package skylands.gametest

import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.level.block.entity.BlockEntity
import skylands.block.BeanstalkBlock
import skylands.blockentity.BeanPlantBlockEntity
import skylands.registry.{SkylandsBlockEntities, SkylandsBlocks}

// Shared GameTest bodies. Each thin @GameTestHolder class in the fabric/
// and neoforge/ subprojects calls into these from a public-static method.
// Scala 3 auto-generates static forwarders on the companion class for
// object methods, so Java callers see `SkylandsTests.placeBeanBlock(helper)`
// as a plain static method.
//
// Every test must end by calling helper.succeed() — silence is interpreted
// as a timeout by the framework.
object SkylandsTests:

  /** Place a bean block; confirm it lands in the registry-resolved state
    * and that the attached BlockEntity is the expected type. */
  def placeBeanBlock(helper: GameTestHelper): Unit =
    val pos = new BlockPos(1, 2, 1)
    val bean = SkylandsBlocks.BEAN.get()
    helper.setBlock(pos, bean)
    helper.assertBlockPresent(bean, pos)

    val raw: BlockEntity = helper.getBlockEntity(pos)
    if raw == null then
      helper.fail("bean block entity missing", pos)
    else raw match
      case plant: BeanPlantBlockEntity =>
        if plant.getType == SkylandsBlockEntities.BEAN_PLANT.get() then
          helper.succeed()
        else
          helper.fail("block entity type mismatch", pos)
      case other =>
        helper.fail("expected BeanPlantBlockEntity, got " + other.getClass.getName, pos)

  /** Cloud block has no collision and is not solid-render. */
  def placeCloudBlock(helper: GameTestHelper): Unit =
    val pos = new BlockPos(1, 2, 1)
    val cloud = SkylandsBlocks.CLOUD.get()
    helper.setBlock(pos, cloud)
    helper.assertBlockPresent(cloud, pos)

    val state = helper.getBlockState(pos)
    val absPos = helper.absolutePos(pos)
    if state.isSolidRender(helper.getLevel, absPos) then
      helper.fail("cloud should not be solid-render", pos)
    else if !state.getCollisionShape(helper.getLevel, absPos).isEmpty then
      helper.fail("cloud should have empty collision shape", pos)
    else
      helper.succeed()

  /** Beanstalk's `center` boolean property round-trips through setValue/getValue. */
  def beanstalkCenterPropertyRoundTrips(helper: GameTestHelper): Unit =
    val pos = new BlockPos(1, 2, 1)
    val beanstalk = SkylandsBlocks.BEANSTALK.get()

    val nonCenter = beanstalk.defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.FALSE)
    helper.setBlock(pos, nonCenter)
    val firstValue: Boolean = helper.getBlockState(pos).getValue(BeanstalkBlock.CENTER).booleanValue()

    val center = beanstalk.defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.TRUE)
    helper.setBlock(pos, center)
    val secondValue: Boolean = helper.getBlockState(pos).getValue(BeanstalkBlock.CENTER).booleanValue()

    if firstValue then
      helper.fail("expected CENTER=false after setValue(false)", pos)
    else if !secondValue then
      helper.fail("expected CENTER=true after setValue(true)", pos)
    else
      helper.succeed()
