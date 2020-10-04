package de.lolhens.minecraft.skylandsmod.block

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.{Block, Material}
import net.minecraft.sound.BlockSoundGroup

class BeanBlock extends Block(BeanBlock.settings) {

}

object BeanBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.CACTUS)
      .hardness(0.8F)
      .resistance(3)
      .sounds(BlockSoundGroup.WOOD)
}
