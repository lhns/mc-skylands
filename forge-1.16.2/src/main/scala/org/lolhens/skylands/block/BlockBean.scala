package org.lolhens.skylands.block

import net.minecraft.block.material.Material
import net.minecraft.block.{AbstractBlock, Block, ITileEntityProvider, SoundType}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockReader
import org.lolhens.skylands.tileentities.TileEntityBeanPlant

/**
 * Created by pierr on 14.01.2017.
 */
class BlockBean extends Block(BlockBean.settings) with ITileEntityProvider {
  //setUnlocalizedName("skylandsmod:bean")
  //setCreativeTab(CreativeTabs.MISC)

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = new TileEntityBeanPlant()
}

object BlockBean {
  val settings: AbstractBlock.Properties =
    AbstractBlock.Properties
      .create(Material.CACTUS)
      .hardnessAndResistance(0.8f, 3)
      .sound(SoundType.WOOD)
}
