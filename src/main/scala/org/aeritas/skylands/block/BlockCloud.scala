package org.aeritas.skylands.block

import net.minecraft.block.Block
import net.minecraft.block.material.{MapColor, Material}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.aeritas.skylands.SkylandsMod
import org.aeritas.skylands.block.BlockCloud.MaterialCloud
import org.aeritas.skylands.world.SimpleTeleporter

class BlockCloud extends Block(MaterialCloud) {
  setUnlocalizedName("cloud")
  setRegistryName(SkylandsMod.ModId, "cloud")

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: net.minecraft.util.EnumHand, facing: net.minecraft.util.EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote && playerIn.isInstanceOf[EntityPlayerMP]) {
      val player = playerIn.asInstanceOf[EntityPlayerMP]
      val dimension = if (player.dimension == 0) SkylandsMod.instance.skylandsDimensionType.getId else 0
      player.server.getPlayerList.transferPlayerToDimension(player, dimension, new SimpleTeleporter(player.server.getWorld(dimension)))
      true
    } else false
  }
}

object BlockCloud {
  object MaterialCloud extends Material(MapColor.SNOW) {
    setReplaceable()
  }
}