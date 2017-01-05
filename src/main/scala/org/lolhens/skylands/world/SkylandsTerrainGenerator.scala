package org.lolhens.skylands.world

import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer

import scala.util.Random

/**
  * Created by pierr on 05.01.2017.
  */
class SkylandsTerrainGenerator(world: World, random: Random) extends TerrainGenerator(world, random) {
  val chunkProviderSky = new ChunkProviderSky(world, random.self)

  override def generate(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit = chunkProviderSky.provideChunk(chunkX, chunkZ, primer)

  override def populate(chunkX: Int, chunkZ: Int): Unit = chunkProviderSky.populate(chunkX, chunkZ)
}
