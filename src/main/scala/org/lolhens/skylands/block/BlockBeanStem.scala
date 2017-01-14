package org.lolhens.skylands.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
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

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, BlockBeanStem.isCenter)

  override def getMetaFromState(state: IBlockState): Int = 0
  override def getStateFromMeta(meta: Int): IBlockState = this.getDefaultState
}

object BlockBeanStem {
  val isCenter: PropertyBool = PropertyBool.create("center")
}