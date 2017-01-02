package org.lolhens.skylands.blocks

import net.minecraft.block.{Block, SoundType}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.World

/**
  * Created by pierr on 02.01.2017.
  */
class BlockPortal extends Block(Material.PORTAL) {
  setUnlocalizedName("skylandsmod:portal")
  setCreativeTab(CreativeTabs.MISC)
  setHardness(2)
  setResistance(1000)
  setSoundType(SoundType.STONE)

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, heldItem: ItemStack, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote && !playerIn.isSneaking)
      false
    false
      /*worldIn.provider.getDimension match {
        case Config
      }*/
  }
}
