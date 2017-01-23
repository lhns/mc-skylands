package org.lolhens.skylands

import java.io.File

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.model.ModelLoader

/**
  * Created by pierr on 02.01.2017.
  */
class SkylandsClient(configFile: File) extends Skylands(configFile) {
  registerModel(Item.getItemFromBlock(blockBeanStem), 0)
  registerModel(Item.getItemFromBlock(blockBean), 0)
  registerModel(Item.getItemFromBlock(blockCloud), 0)

  def registerModel(item: Item, meta: Int): Unit =
    ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName, "inventory"))
}
