package org.lolhens.skylands.world

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.chunk.IChunkGenerator
import net.minecraft.world.{DimensionType, WorldProvider}
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.world.chunk.ChunkProviderSkylands

/**
  * Created by pierr on 01.01.2017.
  */
class WorldProviderSkylands extends WorldProvider {
  override def getDimensionType: DimensionType = SkylandsMod.skylands.skylandsDimensionType

  override def createChunkGenerator(): IChunkGenerator = new ChunkProviderSkylands(worldObj)

  override def canRespawnHere: Boolean = false

  override def getHorizon: Double = Double.NegativeInfinity

  override def getWorldTime: Long = 6000

  //override def getSkyColor(cameraEntity: Entity, partialTicks: Float): Vec3d = new Vec3d(1, 1, 1)

  //override def getFogColor(p_76562_1_ : Float, p_76562_2_ : Float): Vec3d = new Vec3d(1, 1, 1)

  override def getCloudHeight: Float = -20

  //override def isDaytime: Boolean = true
}
