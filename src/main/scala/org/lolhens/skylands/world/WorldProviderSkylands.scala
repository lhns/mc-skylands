package org.lolhens.skylands.world

import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.{DimensionType, WorldProvider}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.world.chunk.ChunkProviderSkylands

/**
  * Created by pierr on 01.01.2017.
  */
class WorldProviderSkylands extends WorldProvider {
  override def getDimensionType: DimensionType = SkylandsMod.skylands.skylandsDimensionType

  override def createChunkGenerator(): IChunkGenerator = new ChunkProviderSkylands(world)

  override def canRespawnHere: Boolean = false

  override def getHorizon: Double = Double.NegativeInfinity

  override def getWorldTime: Long = 6000

  override def getCloudHeight: Float = -20

  override def updateWeather(): Unit = {
    super.updateWeather()
    clearWeather()
  }

  private def clearWeather(): Unit = {
    world.rainingStrength = 0
    world.thunderingStrength = 0
  }
}
