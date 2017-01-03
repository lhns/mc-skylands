package org.lolhens.skylands.world

import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer

import scala.util.Random

/**
  * Created by pierr on 01.01.2017.
  */
class SkylandsTerrainGenerator(world: World, random: Random) {

  def generate(x: Int, z: Int, primer: ChunkPrimer): Unit = {
    for (
      cX <- 0 until 16;
      cZ <- 0 until 16;
      cY <- 0 to 64
    ) {
      primer.setBlockState(cX, cY, cZ, Blocks.STONE.getDefaultState)
    }
  }
}
