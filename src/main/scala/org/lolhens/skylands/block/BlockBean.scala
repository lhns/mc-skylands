package org.lolhens.skylands.block

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{BlockContainer, SoundType}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.world.World
import org.lolhens.skylands.tileentities.TileEntityBeanPlant

/**
  * Created by pierr on 14.01.2017.
  */
class BlockBean extends BlockContainer(Material.CACTUS) {
  setUnlocalizedName("skylandsmod:bean")
  setCreativeTab(CreativeTabs.MISC)
  setHardness(0.8f)
  setResistance(3)
  setSoundType(SoundType.WOOD)

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityBeanPlant()

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL
}
