package de.lolhens.minecraft.skylandsmod.block

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.{Block, BlockState, Material}
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty

class BeanstalkBlock extends Block(BeanstalkBlock.settings) {
  def getState(center: Boolean): BlockState =
    getStateManager.getDefaultState.`with`(BeanstalkBlock.CENTER, java.lang.Boolean.valueOf(center))

  setDefaultState(getState(center = false))

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(BeanstalkBlock.CENTER)
}

object BeanstalkBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.CACTUS)
      .hardness(0.8F)
      .resistance(3)
      .sounds(BlockSoundGroup.GRASS)

  val CENTER: BooleanProperty = BooleanProperty.of("center")
}
