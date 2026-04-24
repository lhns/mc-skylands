package skylands.registry

import net.minecraft.core.registries.Registries
import net.minecraft.resources.{ResourceKey, ResourceLocation}
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.{DimensionType, LevelStem}
import skylands.SkylandsCommon.ModId
import skylands.platform.SkylandsPlatform
import skylands.worldgen.SkylandsChunkGenerator

object SkylandsWorldgen:
  val SKYLANDS_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(ModId, "skylands")

  val SKYLANDS_LEVEL: ResourceKey[Level] = ResourceKey.create(Registries.DIMENSION, SKYLANDS_ID)
  val SKYLANDS_LEVEL_STEM: ResourceKey[LevelStem] = ResourceKey.create(Registries.LEVEL_STEM, SKYLANDS_ID)
  val SKYLANDS_DIMENSION_TYPE: ResourceKey[DimensionType] = ResourceKey.create(Registries.DIMENSION_TYPE, SKYLANDS_ID)

  def register(): Unit =
    SkylandsPlatform.current.registerChunkGenerator("skylands", SkylandsChunkGenerator.CODEC)
