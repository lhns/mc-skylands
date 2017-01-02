package org.lolhens.skylands

import net.minecraft.world.chunk.IChunkGenerator
import net.minecraft.world.{DimensionType, WorldProvider}

/**
  * Created by pierr on 01.01.2017.
  */
class WorldProviderSkylands extends WorldProvider {
  override def getDimensionType: DimensionType = SkylandsMod.skylands.dimensionType

  override def createChunkGenerator(): IChunkGenerator = new ChunkProviderSkylands(worldObj)
}
