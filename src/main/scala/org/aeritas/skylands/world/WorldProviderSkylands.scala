package org.aeritas.skylands.world

import net.minecraft.world.{DimensionType, WorldProvider}
import net.minecraft.world.gen.IChunkGenerator
import org.aeritas.skylands.SkylandsMod
import org.aeritas.skylands.world.chunk.ChunkProviderSkylands

class WorldProviderSkylands extends WorldProvider {
  override def createChunkGenerator(): IChunkGenerator = new ChunkProviderSkylands(world)
  
  override def getDimensionType: DimensionType = SkylandsMod.instance.skylandsDimensionType
  
  override def getWorldTime: Long = 6000L // Always noon
  
  override def canRespawnHere: Boolean = false
  
  override def isSurfaceWorld: Boolean = true
}