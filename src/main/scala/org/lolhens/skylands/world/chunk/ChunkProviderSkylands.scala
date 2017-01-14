package org.lolhens.skylands.world.chunk

import java.util

import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.passive.EntityChicken
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.{Chunk, ChunkPrimer, IChunkGenerator}
import net.minecraft.world.{World, WorldEntitySpawner}

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * Created by pierr on 01.01.2017.
  */
class ChunkProviderSkylands(world: World) extends IChunkGenerator {
  private val random = new Random(world.getSeed + 0x4f9939f508L)
  private val mobs = List(new Biome.SpawnListEntry(classOf[EntityChicken], 100, 1, 4))
  private val terrainGenerator = new SkylandsTerrainGenerator(world, random)

  override def getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): util.List[SpawnListEntry] = creatureType match {
    case EnumCreatureType.CREATURE => mobs
    case EnumCreatureType.AMBIENT => Nil
    case EnumCreatureType.MONSTER => Nil
    case EnumCreatureType.WATER_CREATURE => Nil
  }

  override def recreateStructures(chunkIn: Chunk, x: Int, z: Int): Unit = ()

  override def generateStructures(chunkIn: Chunk, x: Int, z: Int): Boolean = false

  override def getStrongholdGen(worldIn: World, structureName: String, position: BlockPos): BlockPos = null

  override def provideChunk(chunkX: Int, chunkZ: Int): Chunk = {
    val chunkPrimer = new ChunkPrimer()

    //val biomesForGeneration = world.getBiomeProvider.getBiomesForGeneration(Array[Biome](), chunkX * 16, chunkZ * 16, 16, 16)

    terrainGenerator.generate(chunkX, chunkZ, chunkPrimer)

    val chunk = new Chunk(world, chunkPrimer, chunkX, chunkZ)

    /*val biomeArray = chunk.getBiomeArray
    for (i <- 0 until biomeArray.length)
      biomeArray(i) = Biome.getIdForBiome(biomesForGeneration(i)).toByte*/

    chunk.generateSkylightMap()

    chunk
  }

  override def populate(chunkX: Int, chunkZ: Int): Unit = {
    val (x, z) = (chunkX * 16, chunkZ * 16)

    val blockPos = new BlockPos(x, 0, z)
    val biome = world.getBiome(blockPos.add(16, 0, 16))
    //System.out.print(biome.getBiomeName)

    terrainGenerator.populate(chunkX, chunkZ)

    //biome.decorate(worldObj, random.self, blockPos)

    WorldEntitySpawner.performWorldGenSpawning(world, biome, x + 8, x + 8, 16, 16, random.self)
  }
}
