package org.lolhens.skylands.world.chunk

import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer

import scala.util.Random

/**
  * Created by pierr on 05.01.2017.
  */
abstract class TerrainGenerator(val world: World, val random: Random) {
  def generate(chunkX: Int, chunkZ: Int, primer: ChunkPrimer): Unit

  def populate(chunkX: Int, chunkZ: Int): Unit = ()
}
