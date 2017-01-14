package org.lolhens.skylands.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.{Block, SoundType}
import net.minecraft.creativetab.CreativeTabs

/**
  * Created by pierr on 14.01.2017.
  */
class BlockBeanStem extends Block(Material.CACTUS) {
  setUnlocalizedName("skylandsmod:beanstem")
  setCreativeTab(CreativeTabs.MISC)
  setHardness(0.8f)
  setResistance(3)
  setSoundType(SoundType.WOOD)
}
