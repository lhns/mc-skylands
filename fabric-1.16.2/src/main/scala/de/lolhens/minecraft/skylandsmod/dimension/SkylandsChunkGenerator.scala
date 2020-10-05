package de.lolhens.minecraft.skylandsmod.dimension

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.{ChunkGenerator, StructuresConfig, VerticalBlockSample}
import net.minecraft.world.{BlockView, ChunkRegion, Heightmap, WorldAccess}

class SkylandsChunkGenerator(biomeSource: BiomeSource) extends ChunkGenerator(biomeSource, new StructuresConfig(false)) {
  override def getCodec: Codec[_ <: ChunkGenerator] = SkylandsChunkGenerator.CODEC

  override def withSeed(seed: Long): ChunkGenerator = this

  override def buildSurface(region: ChunkRegion, chunk: Chunk): Unit = ()

  override def populateNoise(world: WorldAccess, accessor: StructureAccessor, chunk: Chunk): Unit = ()

  override def getHeight(x: Int, z: Int, heightmapType: Heightmap.Type): Int = 0

  override def getColumnSample(x: Int, z: Int): BlockView = new VerticalBlockSample(Array())
}

object SkylandsChunkGenerator {
  val CODEC: Codec[SkylandsChunkGenerator] = RecordCodecBuilder.create(instance =>
    instance.group(
      BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator => generator.biomeSource)
    ).apply(instance, instance.stable[java.util.function.Function[BiomeSource, SkylandsChunkGenerator]] { biomeSource =>
      new SkylandsChunkGenerator(biomeSource)
    }))
}
