package org.aeritas.skylands.integration.bop

import biomesoplenty.api.biome.BOPBiomes
import net.minecraft.world.biome.Biome
import net.minecraftforge.fml.common.Optional

@Optional.Interface(iface = "biomesoplenty.api.biome.BOPBiomes", modid = "biomesoplenty")
object BiomesOPlentyIntegration {
  private var initialized = false
  private var bopBiomes: Map[Int, Biome] = Map.empty

  @Optional.Method(modid = "biomesoplenty")
  def initialize(): Unit = {
    if (!initialized) {
      initialized = true
      loadBiomes()
    }
  }

  @Optional.Method(modid = "biomesoplenty")
  private def loadBiomes(): Unit = {
    bopBiomes = Seq(
      BOPBiomes.alps,
      BOPBiomes.alps_foothills,
      BOPBiomes.bamboo_forest,
      BOPBiomes.bayou,
      BOPBiomes.bog,
      BOPBiomes.boreal_forest,
      BOPBiomes.brushland,
      BOPBiomes.chaparral,
      BOPBiomes.cherry_blossom_grove,
      BOPBiomes.cold_desert,
      BOPBiomes.coniferous_forest,
      BOPBiomes.crag,
      BOPBiomes.dead_forest,
      BOPBiomes.eucalyptus_forest,
      BOPBiomes.fen,
      BOPBiomes.flower_field,
      BOPBiomes.glacier,
      BOPBiomes.grassland,
      BOPBiomes.grove,
      BOPBiomes.highland,
      BOPBiomes.land_of_lakes,
      BOPBiomes.lavender_fields,
      BOPBiomes.lush_desert,
      BOPBiomes.lush_swamp,
      BOPBiomes.maple_woods,
      BOPBiomes.marsh,
      BOPBiomes.meadow,
      BOPBiomes.moor,
      BOPBiomes.mountain,
      BOPBiomes.mystic_grove,
      BOPBiomes.oasis,
      BOPBiomes.ominous_woods,
      BOPBiomes.origin_beach,
      BOPBiomes.outback,
      BOPBiomes.overgrown_cliffs,
      BOPBiomes.prairie,
      BOPBiomes.rainforest,
      BOPBiomes.redwood_forest,
      BOPBiomes.sacred_springs,
      BOPBiomes.seasonal_forest,
      BOPBiomes.shield,
      BOPBiomes.shrubland,
      BOPBiomes.snowy_coniferous_forest,
      BOPBiomes.snowy_forest,
      BOPBiomes.steppe,
      BOPBiomes.temperate_rainforest,
      BOPBiomes.tropical_rainforest,
      BOPBiomes.tundra,
      BOPBiomes.wasteland,
      BOPBiomes.wetland,
      BOPBiomes.woodland,
      BOPBiomes.xeric_shrubland
    ).flatMap(opt => opt.map(biome => biome.getIdForBiome(biome) -> biome)).toMap
  }

  def getBiome(id: Int): Option[Biome] = bopBiomes.get(id)
  
  def isInitialized: Boolean = initialized
  
  def getBiomes: Map[Int, Biome] = bopBiomes
}