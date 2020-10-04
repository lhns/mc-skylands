package org.lolhens.skylands.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.block.{AbstractBlock, Block, SoundType}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.state.BooleanProperty

/**
  * Created by pierr on 14.01.2017.
  */
class BlockBeanStem extends Block(BlockBean.settings) {
  //setUnlocalizedName("skylandsmod:beanstem")
  //setCreativeTab(CreativeTabs.MISC)

  setDefaultState(blockState.getBaseState.withProperty(BlockBeanStem.isCenter, Boolean.box(false)))

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, BlockBeanStem.isCenter)

  override def getMetaFromState(state: IBlockState): Int = if (state.getValue(BlockBeanStem.isCenter).booleanValue()) 1 else 0

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(BlockBeanStem.isCenter, Boolean.box(meta == 1))
}

object BlockBeanStem {
  val isCenter: BooleanProperty = BooleanProperty.create("center")
}