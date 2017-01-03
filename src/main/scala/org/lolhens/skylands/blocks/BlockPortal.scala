package org.lolhens.skylands.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{Teleporter, World}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.world.SkylandsTeleporter

/**
  * Created by pierr on 02.01.2017.
  */
class BlockPortal extends Block(Material.PORTAL) {
  setUnlocalizedName("skylandsmod:portal")
  setCreativeTab(CreativeTabs.MISC)
  setHardness(2)
  setResistance(1000)
  setSoundType(SoundType.STONE)

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, heldItem: ItemStack, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    player match {
      case player: EntityPlayerMP if !player.isSneaking =>
        val skylandsDimensionId: Int = SkylandsMod.skylands.dimensionType.getId

        val targetDimensionId: Int = world.provider.getDimension match {
          case `skylandsDimensionId` => 0
          case dimensionId => skylandsDimensionId
        }

        val teleporter: Teleporter = new SkylandsTeleporter(player.getServer.worldServerForDimension(targetDimensionId), pos)
        player.getServer.getPlayerList.transferPlayerToDimension(player, targetDimensionId, teleporter)
        true

      case player if !player.isSneaking =>
        false

      case player =>
        super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ)
    }
  }
}
