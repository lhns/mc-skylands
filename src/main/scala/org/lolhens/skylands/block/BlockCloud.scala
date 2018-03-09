package org.lolhens.skylands.block

import net.minecraft.block.material.{MapColor, Material}
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.block.{Block, SoundType}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.util.EnumFacing.Axis
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{BlockRenderLayer, EnumFacing}
import net.minecraft.world.{DimensionType, IBlockAccess, Teleporter, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.block.BlockCloud.MaterialCloud
import org.lolhens.skylands.world.SimpleTeleporter

/**
  * Created by pierr on 02.01.2017.
  */
class BlockCloud extends Block(MaterialCloud) {
  setUnlocalizedName("skylandsmod:cloud")
  setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
  setHardness(1)
  setResistance(2)
  setSoundType(SoundType.SNOW)
  setLightLevel(0.1f)

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this)

  @SideOnly(Side.CLIENT)
  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.TRANSLUCENT

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  override def shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean = {
    val vertical = side.getAxis == Axis.Y

    def isCloud(side: EnumFacing) = blockAccess.getBlockState(pos.offset(side)).getBlock == this

    def isHidden(side: EnumFacing) = !isCloud(side) && !blockAccess.isAirBlock(pos.offset(side))

    vertical ||
      !isCloud(side) ||
      isHidden(side.rotateY()) ||
      isHidden(side.rotateY().rotateY()) ||
      isHidden(side.rotateY().rotateY().rotateY())
  }

  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB = Block.NULL_AABB

  override def onEntityCollidedWithBlock(world: World, position: BlockPos, state: IBlockState, entity: Entity): Unit = {
    if (entity.motionY < 0) entity.motionY *= 0.5 //entity.motionY = Math.max(entity.motionY, -0.3)
    entity.fallDistance = 0

    val isFlying = entity match {
      case player: EntityPlayer => player.capabilities.isFlying
      case _ => false
    }

    if (!isFlying && entity.motionY <= 0.1) entity.onGround = true

    def nearCloud(position: BlockPos, radius: Int): Boolean = {
      for (
        x <- -radius to radius;
        z <- -radius to radius
      ) yield position.add(x, 0, z)
    }.exists(world.getBlockState(_).getBlock == SkylandsMod.skylands.blockBeanStem)

    entity match {
      case player: EntityPlayerMP if !player.world.isRemote =>
        val playerPos = new BlockPos(player)

        val teleportTarget: Option[(DimensionType, BlockPos)] =
          if (player.dimension == DimensionType.OVERWORLD.getId && position.getY >= 250 && nearCloud(playerPos, 20))
            Some(SkylandsMod.skylands.skylandsDimensionType -> playerPos.add(0, SkylandsMod.skylands.skylandsOverlap - 255, 0))
          else
            None

        for ((dimension, position) <- teleportTarget) {
          val teleporter: Teleporter = new SimpleTeleporter(player.getServer.getWorld(dimension.getId), Some(position))
          player.getServer.getPlayerList.transferPlayerToDimension(player, dimension.getId, teleporter)
        }

      case _ =>
    }
  }
}

object BlockCloud {

  object MaterialCloud extends Material(MapColor.CLOTH) {
    override def getCanBurn: Boolean = true
  }

}
