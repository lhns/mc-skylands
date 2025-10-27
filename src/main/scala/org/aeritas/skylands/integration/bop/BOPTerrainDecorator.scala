package org.aeritas.skylands.integration.bop

import biomesoplenty.api.block.BOPBlocks
import biomesoplenty.common.block.BlockBOPGrass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraftforge.fml.common.Optional

@Optional.Interface(iface = "biomesoplenty.api.block.BOPBlocks", modid = "biomesoplenty")
class BOPTerrainDecorator(world: World) {
  @Optional.Method(modid = "biomesoplenty")
  def getTopBlock(biome: Biome, pos: BlockPos): IBlockState = {
    biome match {
      case _ if biome.getRegistryName.getNamespace == "biomesoplenty" =>
        getBOPTopBlock(biome, pos)
      case _ =>
        getVanillaTopBlock(biome, pos)
    }
  }

  @Optional.Method(modid = "biomesoplenty")
  private def getBOPTopBlock(biome: Biome, pos: BlockPos): IBlockState = {
    val random = world.rand
    // Customize BOP biome top blocks based on biome type and characteristics
    biome match {
      case _ if isForestBiome(biome) => 
        if (random.nextFloat() < 0.1) BOPBlocks.grass.getDefaultState.withProperty(BlockBOPGrass.VARIANT, BlockBOPGrass.BOPGrassType.LOAMY)
        else Blocks.GRASS.getDefaultState
      case _ if isAlpineBiome(biome) =>
        if (pos.getY > 100) Blocks.SNOW.getDefaultState
        else Blocks.GRASS.getDefaultState
      case _ if isDesertBiome(biome) =>
        if (random.nextFloat() < 0.2) BOPBlocks.white_sand.getDefaultState
        else Blocks.SAND.getDefaultState
      case _ =>
        Blocks.GRASS.getDefaultState
    }
  }

  private def getVanillaTopBlock(biome: Biome, pos: BlockPos): IBlockState = {
    Blocks.GRASS.getDefaultState
  }

  private def isForestBiome(biome: Biome): Boolean = {
    biome.getBiomeName.toLowerCase.contains("forest")
  }

  private def isAlpineBiome(biome: Biome): Boolean = {
    biome.getBiomeName.toLowerCase.contains("alps") || 
    biome.getBiomeName.toLowerCase.contains("mountain") ||
    biome.getBiomeName.toLowerCase.contains("peak")
  }

  private def isDesertBiome(biome: Biome): Boolean = {
    biome.getBiomeName.toLowerCase.contains("desert") ||
    biome.getBiomeName.toLowerCase.contains("wasteland") ||
    biome.getBiomeName.toLowerCase.contains("outback")
  }
}