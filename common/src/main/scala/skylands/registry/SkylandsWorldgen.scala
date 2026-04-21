package skylands.registry

import com.mojang.serialization.MapCodec
import dev.architectury.registry.registries.{DeferredRegister, RegistrySupplier}
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.{DimensionType, LevelStem}
import skylands.SkylandsCommon.ModId
import skylands.worldgen.SkylandsChunkGenerator

object SkylandsWorldgen:
  val SKYLANDS_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(ModId, "skylands")

  val SKYLANDS_LEVEL: ResourceKey[Level] = ResourceKey.create(Registries.DIMENSION, SKYLANDS_ID)
  val SKYLANDS_LEVEL_STEM: ResourceKey[LevelStem] = ResourceKey.create(Registries.LEVEL_STEM, SKYLANDS_ID)
  val SKYLANDS_DIMENSION_TYPE: ResourceKey[DimensionType] = ResourceKey.create(Registries.DIMENSION_TYPE, SKYLANDS_ID)

  val CHUNK_GENERATORS: DeferredRegister[MapCodec[? <: ChunkGenerator]] =
    DeferredRegister.create(ModId, Registries.CHUNK_GENERATOR)

  val SKYLANDS: RegistrySupplier[MapCodec[? <: ChunkGenerator]] =
    CHUNK_GENERATORS.register("skylands", () => SkylandsChunkGenerator.CODEC)

  def register(): Unit = CHUNK_GENERATORS.register()
