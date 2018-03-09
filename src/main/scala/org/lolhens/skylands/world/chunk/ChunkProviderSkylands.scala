package org.lolhens.skylands.world.chunk

import java.util

import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.passive.EntityChicken
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.{Chunk, ChunkPrimer}
import net.minecraft.world.gen.IChunkGenerator

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by pierr on 01.01.2017.
  */
class ChunkProviderSkylands(world: World) extends IChunkGenerator {
  private val random = new Random(world.getSeed * 0x4f9939f508L)
  private val mobs = List(new Biome.SpawnListEntry(classOf[EntityChicken], 100, 1, 4))
  private val terrainGenerator = new SkylandsTerrainGenerator(world, random)

  override def getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): util.List[SpawnListEntry] =
    (creatureType match {
      case EnumCreatureType.CREATURE => mobs
      case EnumCreatureType.AMBIENT => Nil
      case EnumCreatureType.MONSTER => Nil
      case EnumCreatureType.WATER_CREATURE => Nil
    }).toBuffer.asJava

  override def recreateStructures(chunkIn: Chunk, x: Int, z: Int): Unit = ()

  override def generateStructures(chunkIn: Chunk, x: Int, z: Int): Boolean = false

  //override def getStrongholdGen(worldIn: World, structureName: String, position: BlockPos): BlockPos = null

  override def generateChunk(chunkX: Int, chunkZ: Int): Chunk = {
    val chunkPrimer = new ChunkPrimer()
    val offsetPrimer = new OffsetChunkPrimer(chunkPrimer, 64)

    terrainGenerator.generate(chunkX, chunkZ, offsetPrimer)

    val chunk = new Chunk(world, chunkPrimer, chunkX, chunkZ)

    //chunk.setLightPopulated(false)

    //chunk.generateSkylightMap3()
    chunk.generateSkylightMap()

    /*for (
      x <- 0 until 16;
      y <- 0 until 256;
      z <- 0 until 16
    ) {
      val pos = new BlockPos(x, y, z)
      /*val enumSkyBlock: EnumSkyBlock =
        if (chunk.getBlockState(pos).getBlock == Blocks.AIR)
        EnumSkyBlock.SKY
      else
      EnumSkyBlock.BLOCK*/
      //if (chunk.canSeeSky(pos))
        chunk.setLightFor(EnumSkyBlock.SKY, pos, 15)
      //else
        //chunk.setLightFor(EnumSkyBlock.SKY, pos, 0)
    }*/



    chunk
  }

  override def populate(chunkX: Int, chunkZ: Int): Unit = {
    val (x, z) = (chunkX * 16, chunkZ * 16)

    val blockPos = new BlockPos(x, 0, z)
    val biome = world.getBiome(blockPos.add(16, 0, 16))

    terrainGenerator.populate(chunkX, chunkZ)

    //biome.decorate(worldObj, random.self, blockPos)
  }

  override def isInsideStructure(world: World, s: String, blockPos: BlockPos): Boolean = false

  override def getNearestStructurePos(world: World, s: String, blockPos: BlockPos, b: Boolean): BlockPos = null
}
