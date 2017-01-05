package org.lolhens.skylands.world

import java.util

import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.passive.EntityChicken
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.{Chunk, IChunkGenerator}

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * Created by pierr on 01.01.2017.
  */
class ChunkProviderSkylands(worldObj: World) extends IChunkGenerator {
  private val random = new Random(worldObj.getSeed)
  private val mobs = List(new Biome.SpawnListEntry(classOf[EntityChicken], 100, 1, 4))
  private val terrainGenerator = new SkylandsTerrainGenerator(worldObj, random)

  override def getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): util.List[SpawnListEntry] = creatureType match {
    case EnumCreatureType.CREATURE => mobs
    case EnumCreatureType.AMBIENT => Nil
    case EnumCreatureType.MONSTER => Nil
    case EnumCreatureType.WATER_CREATURE => Nil
  }

  override def recreateStructures(chunkIn: Chunk, x: Int, z: Int): Unit = ()

  override def generateStructures(chunkIn: Chunk, x: Int, z: Int): Boolean = false

  override def getStrongholdGen(worldIn: World, structureName: String, position: BlockPos): BlockPos = null

  override def provideChunk(x: Int, z: Int): Chunk = {
    /*val chunkPrimer = new ChunkPrimer()

    val biomesForGeneration = worldObj.getBiomeProvider.getBiomesForGeneration(Array[Biome](), x * 16, z * 16, 16, 16)

    terrainGenerator.generate(x, z, chunkPrimer)

    val chunk = new Chunk(worldObj, chunkPrimer, x, z)

    val biomeArray = chunk.getBiomeArray
    for (i <- 0 until biomeArray.length)
      biomeArray(i) = Biome.getIdForBiome(biomesForGeneration(i)).toByte

    chunk.generateSkylightMap()

    chunk*/
    new ChunkProviderSky(worldObj).provideChunk(x, z)
  }

  override def populate(x: Int, z: Int): Unit = {
    val i = x * 16
    val j = z * 16
    val blockPos = new BlockPos(i, 0, j)
    val biome = worldObj.getBiome(blockPos.add(16, 0, 16))
    System.out.print(biome.getBiomeName)
    //biome.decorate(worldObj, random.self, blockPos)
    //WorldEntitySpawner.performWorldGenSpawning(worldObj, biome, i + 8, j + 8, 16, 16, random.self)
  }
}
