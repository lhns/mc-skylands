package org.aeritas.skylands.world.chunk

import net.minecraft.world.World
import net.minecraft.world.chunk.{Chunk, ChunkPrimer}
import net.minecraft.world.gen.ChunkGeneratorOverworld
import org.aeritas.skylands.generator.CloudGenerator

class ChunkProviderSkylands(world: World) extends ChunkGeneratorOverworld(world, world.getSeed, world.getWorldInfo.isMapFeaturesEnabled, "") {
  private val terrainGenerator = new SkylandsTerrainGenerator(world)
  private val cloudGenerator = new CloudGenerator(world.getSeed)

  override def generateChunk(x: Int, z: Int): Chunk = {
    val primer = new ChunkPrimer()
    terrainGenerator.generate(x, z, primer)
    cloudGenerator.generate(x, z, primer)
    val chunk = new Chunk(world, primer, x, z)
    chunk.generateSkylightMap()
    chunk
  }
}