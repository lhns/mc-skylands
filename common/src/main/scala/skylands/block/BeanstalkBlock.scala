package skylands.block

import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{Block, SoundType}
import net.minecraft.world.level.material.MapColor

class BeanstalkBlock extends Block(BeanstalkBlock.Properties):
  registerDefaultState(defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.FALSE))

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(BeanstalkBlock.CENTER)

  def stateForCenter(center: Boolean): BlockState =
    defaultBlockState().setValue(BeanstalkBlock.CENTER, java.lang.Boolean.valueOf(center))

object BeanstalkBlock:
  val CENTER: BooleanProperty = BooleanProperty.create("center")

  private val Properties: BlockBehaviour.Properties =
    BlockBehaviour.Properties.of()
      .mapColor(MapColor.WOOD)
      .strength(0.8f, 3f)
      .sound(SoundType.WOOD)
