package org.lolhens.skylands.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.*;

import java.util.Random;

public class ChunkProviderSky {
    private Random random;
    private NoiseGeneratorOctaves lperlinNoise1;
    private NoiseGeneratorOctaves lperlinNoise2;
    private NoiseGeneratorOctaves perlinNoise1;
    private NoiseGeneratorOctaves noise10;
    private NoiseGeneratorOctaves noise11;
    public NoiseGeneratorOctaves noiseGen5;
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorPerlin noise12;
    private World world;
    private double noiseArray[];
    private double pnr[];
    private double ar[];
    private double br[];
    private MapGenBase mapGenCaves = new MapGenCaves();
    private Biome[] biomes;
    private double noise1[];
    private double noisel1[];
    private double noise2[];
    private double noise5[];
    private double noise6[];

    public ChunkProviderSky(World world, Random random) {
        pnr = new double[256];
        ar = new double[256];
        br = new double[256];
        this.world = world;
        this.random = random;
        lperlinNoise1 = new NoiseGeneratorOctaves(random, 16);
        lperlinNoise2 = new NoiseGeneratorOctaves(random, 16);
        perlinNoise1 = new NoiseGeneratorOctaves(random, 8);
        noise10 = new NoiseGeneratorOctaves(random, 4);
        noise11 = new NoiseGeneratorOctaves(random, 4);
        noiseGen5 = new NoiseGeneratorOctaves(random, 10);
        noiseGen6 = new NoiseGeneratorOctaves(random, 16);
        noise12 = new NoiseGeneratorPerlin(random, 8);
    }

    private void generateStone(int chunkX, int chunkZ, ChunkPrimer primer) {
        noiseArray = getNoiseArray(noiseArray, chunkX * 2, 0, chunkZ * 2, 3, 33, 3);
        for (int i1 = 0; i1 < 2; i1++) {
            for (int j1 = 0; j1 < 2; j1++) {
                for (int k1 = 0; k1 < 32; k1++) {
                    double d = 0.25D;
                    double d1 = noiseArray[((i1) * 3 + (j1)) * 33 + k1];
                    double d2 = noiseArray[((i1) * 3 + (j1 + 1)) * 33 + k1];
                    double d3 = noiseArray[((i1 + 1) * 3 + (j1)) * 33 + k1];
                    double d4 = noiseArray[((i1 + 1) * 3 + (j1 + 1)) * 33 + k1];
                    double d5 = (noiseArray[((i1) * 3 + (j1)) * 33 + (k1 + 1)] - d1) * d;
                    double d6 = (noiseArray[((i1) * 3 + (j1 + 1)) * 33 + (k1 + 1)] - d2) * d;
                    double d7 = (noiseArray[((i1 + 1) * 3 + (j1)) * 33 + (k1 + 1)] - d3) * d;
                    double d8 = (noiseArray[((i1 + 1) * 3 + (j1 + 1)) * 33 + (k1 + 1)] - d4) * d;
                    for (int l1 = 0; l1 < 4; l1++) {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        for (int i2 = 0; i2 < 8; i2++) {
                            int x = (j1 * 8);
                            int y = k1 * 4 + l1;
                            int z = (i2 + i1 * 8);
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            for (int k2 = 0; k2 < 8; k2++) {
                                IBlockState blockState = Blocks.AIR.getDefaultState();
                                if (d15 > 0.0D) {
                                    blockState = Blocks.STONE.getDefaultState();
                                }
                                primer.setBlockState(z, y, x, blockState);

                                x++;
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }

                }

            }

        }

    }

    private void replaceBiomeBlocks(int xChunk, int zChunk, ChunkPrimer primer, Biome biomes[]) {
        double d = 0.03125D;
        pnr = noise10.generateNoiseOctaves(pnr, xChunk * 16, zChunk * 16, 0, 16, 16, 1, d, d, 1.0D);
        ar = noise10.generateNoiseOctaves(ar, xChunk * 16, 109 /*109.0134D*/, zChunk * 16, 16, 1, 16, d, 1.0D, d);
        br = noise11.generateNoiseOctaves(br, xChunk * 16, zChunk * 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Biome biome = biomes[x + z * 16];
                int i1 = (int) (br[x + z * 16] / 3D + 3D + random.nextDouble() * 0.25D);
                int j1 = -1;
                IBlockState topBlock = biome.topBlock;
                IBlockState fillerBlock = biome.fillerBlock;
                for (int y = 127; y >= 0; y--) {
                    Block block = primer.getBlockState(x, y, z).getBlock();
                    if (block == Blocks.AIR) {
                        j1 = -1;
                        continue;
                    }
                    if (block != Blocks.STONE) continue;
                    if (j1 == -1) {
                        if (i1 <= 0) {
                            topBlock = Blocks.AIR.getDefaultState();
                            fillerBlock = Blocks.STONE.getDefaultState();
                        }
                        j1 = i1;
                        if (y >= 0) {
                            primer.setBlockState(x, y, z, topBlock);
                        } else {
                            primer.setBlockState(x, y, z, fillerBlock);
                        }
                        continue;
                    }
                    if (j1 <= 0) {
                        continue;
                    }
                    j1--;
                    primer.setBlockState(x, y, z, fillerBlock);
                    if (j1 == 0 && fillerBlock.getBlock() == Blocks.SAND) {
                        j1 = random.nextInt(4);
                        fillerBlock = Blocks.SANDSTONE.getDefaultState();
                    }
                }

            }

        }

    }

    public void provideChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
        biomes = world.getBiomeProvider().getBiomes(biomes, chunkX * 16, chunkZ * 16, 16, 16);
        generateStone(chunkX, chunkZ, primer);
        replaceBiomeBlocks(chunkX, chunkZ, primer, biomes);
        mapGenCaves.generate(world, chunkX, chunkZ, primer);
    }

    private double[] getNoiseArray(double noiseArray[], int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        if (noiseArray == null) {
            noiseArray = new double[sizeX * sizeY * sizeZ];
        }
        double d = 684.41200000000003D;
        double d1 = 684.41200000000003D;

        noise5 = noiseGen5.generateNoiseOctaves(noise5, x, z, sizeX, sizeZ, 1.121D, 1.121D, 0.5D);
        noise6 = noiseGen6.generateNoiseOctaves(noise6, x, z, sizeX, sizeZ, 200D, 200D, 0.5D);
        d *= 2D;
        noise1 = perlinNoise1.generateNoiseOctaves(noise1, x, y, z, sizeX, sizeY, sizeZ, d / 80D, d1 / 160D, d / 80D);
        noisel1 = lperlinNoise1.generateNoiseOctaves(noisel1, x, y, z, sizeX, sizeY, sizeZ, d, d1, d);
        noise2 = lperlinNoise2.generateNoiseOctaves(noise2, x, y, z, sizeX, sizeY, sizeZ, d, d1, d);
        int index = 0;
        for (int j2 = 0; j2 < sizeX; j2++) {
            for (int l2 = 0; l2 < sizeZ; l2++) {
                for (int j3 = 0; j3 < sizeY; j3++) {
                    double d10 = noisel1[index] / 512D;
                    double d11 = noise2[index] / 512D;
                    double d12 = (noise1[index] / 10D + 1.0D) / 2D;

                    double noise;
                    if (d12 < 0.0D)
                        noise = d10;
                    else if (d12 > 1.0D)
                        noise = d11;
                    else
                        noise = d10 + (d11 - d10) * d12;

                    noise -= 8D;
                    int k3 = 32;
                    if (j3 > sizeY - k3) {
                        double d13 = (float) (j3 - (sizeY - k3)) / ((float) k3 - 1.0F);
                        noise = noise * (1.0D - d13) + -30D * d13;
                    }
                    k3 = 8;
                    if (j3 < k3) {
                        double d14 = (float) (k3 - j3) / ((float) k3 - 1.0F);
                        noise = noise * (1.0D - d14) + -30D * d14;
                    }
                    noiseArray[index] = noise;
                    index++;
                }

            }

        }

        return noiseArray;
    }

    public void populate(int chunkX, int chunkZ) {
        BlockSand.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        Biome biome = world.getBiome(new BlockPos(x + 16, 0, z + 16));
        random.setSeed(world.getSeed());
        long l1 = (random.nextLong() / 2L) * 2L + 1L;
        long l2 = (random.nextLong() / 2L) * 2L + 1L;
        random.setSeed((long) chunkX * l1 + (long) chunkZ * l2 ^ world.getSeed());
        double d = 0.25D;
        if (random.nextInt(4) == 0) {
            int i1 = x + random.nextInt(16) + 8;
            int l4 = random.nextInt(128);
            int i8 = z + random.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.WATER)).generate(world, random, new BlockPos(i1, l4, i8));
        }
        if (random.nextInt(8) == 0) {
            int j1 = x + random.nextInt(16) + 8;
            int i5 = random.nextInt(random.nextInt(120) + 8);
            int j8 = z + random.nextInt(16) + 8;
            if (i5 < 64 || random.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.LAVA)).generate(world, random, new BlockPos(j1, i5, j8));
            }
        }
        for (int k1 = 0; k1 < 8; k1++) {
            int j5 = x + random.nextInt(16) + 8;
            int k8 = random.nextInt(128);
            int i13 = z + random.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(world, random, new BlockPos(j5, k8, i13));
        }

        for (int i2 = 0; i2 < 10; i2++) {
            int k5 = x + random.nextInt(16);
            int l8 = random.nextInt(128);
            int j13 = z + random.nextInt(16);
            (new WorldGenClay(32)).generate(world, random, new BlockPos(k5, l8, j13));
        }

        for (int j2 = 0; j2 < 20; j2++) {
            int l5 = x + random.nextInt(16);
            int i9 = random.nextInt(128);
            int k13 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.DIRT.getDefaultState(), 32)).generate(world, random, new BlockPos(l5, i9, k13));
        }

        for (int k2 = 0; k2 < 10; k2++) {
            int i6 = x + random.nextInt(16);
            int j9 = random.nextInt(128);
            int l13 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), 32)).generate(world, random, new BlockPos(i6, j9, l13));
        }

        for (int i3 = 0; i3 < 20; i3++) {
            int j6 = x + random.nextInt(16);
            int k9 = random.nextInt(128);
            int i14 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), 16)).generate(world, random, new BlockPos(j6, k9, i14));
        }

        for (int j3 = 0; j3 < 20; j3++) {
            int k6 = x + random.nextInt(16);
            int l9 = random.nextInt(64);
            int j14 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), 8)).generate(world, random, new BlockPos(k6, l9, j14));
        }

        for (int k3 = 0; k3 < 2; k3++) {
            int l6 = x + random.nextInt(16);
            int i10 = random.nextInt(32);
            int k14 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), 8)).generate(world, random, new BlockPos(l6, i10, k14));
        }

        for (int l3 = 0; l3 < 8; l3++) {
            int i7 = x + random.nextInt(16);
            int j10 = random.nextInt(16);
            int l14 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), 7)).generate(world, random, new BlockPos(i7, j10, l14));
        }

        for (int i4 = 0; i4 < 1; i4++) {
            int j7 = x + random.nextInt(16);
            int k10 = random.nextInt(16);
            int i15 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), 7)).generate(world, random, new BlockPos(j7, k10, i15));
        }

        for (int j4 = 0; j4 < 1; j4++) {
            int k7 = x + random.nextInt(16);
            int l10 = random.nextInt(16) + random.nextInt(16);
            int j15 = z + random.nextInt(16);
            (new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), 6)).generate(world, random, new BlockPos(k7, l10, j15));
        }

        d = 0.5D;
        int k4 = (int) ((noise12.getValue((double) x * d, (double) z * d) / 8D + random.nextDouble() * 4D + 4D) / 3D);

        int numTrees = 0;
        if (random.nextInt(10) == 0) numTrees++;
        if (biome == Biomes.FOREST) numTrees += k4 + 5;
        // rainforest
        if (biome == Biomes.FOREST_HILLS) numTrees += k4 + 5;
        // seasonalForest
        if (biome == Biomes.BIRCH_FOREST) numTrees += k4 + 2;
        if (biome == Biomes.TAIGA) numTrees += k4 + 5;
        if (biome == Biomes.DESERT) numTrees -= 20;
        // tundra
        if (biome == Biomes.COLD_TAIGA) numTrees -= 20;
        if (biome == Biomes.PLAINS) numTrees -= 20;
        for (int i11 = 0; i11 < numTrees; i11++) {
            int k15 = x + random.nextInt(16) + 8;
            int j18 = z + random.nextInt(16) + 8;
            WorldGenAbstractTree worldgenerator = biome.genBigTreeChance(random);
            worldgenerator.generate(world, random, new BlockPos(k15, world.getHeightmapHeight(k15, j18), j18));
        }

        for (int j11 = 0; j11 < 2; j11++) {
            int l15 = x + random.nextInt(16) + 8;
            int k18 = random.nextInt(128);
            int i21 = z + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION)).generate(world, random, new BlockPos(l15, k18, i21));
        }

        if (random.nextInt(2) == 0) {
            int x1 = x + random.nextInt(16) + 8;
            int y1 = random.nextInt(128);
            int z1 = z + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY)).generate(world, random, new BlockPos(x1, y1, z1));
        }
        if (random.nextInt(4) == 0) {
            int l11 = x + random.nextInt(16) + 8;
            int j16 = random.nextInt(128);
            int i19 = z + random.nextInt(16) + 8;
            (new WorldGenBush(Blocks.BROWN_MUSHROOM)).generate(world, random, new BlockPos(l11, j16, i19));
        }
        if (random.nextInt(8) == 0) {
            int i12 = x + random.nextInt(16) + 8;
            int k16 = random.nextInt(128);
            int j19 = z + random.nextInt(16) + 8;
            (new WorldGenBush(Blocks.RED_MUSHROOM)).generate(world, random, new BlockPos(i12, k16, j19));
        }
        for (int j12 = 0; j12 < 10; j12++) {
            int l16 = x + random.nextInt(16) + 8;
            int k19 = random.nextInt(128);
            int j21 = z + random.nextInt(16) + 8;
            (new WorldGenReed()).generate(world, random, new BlockPos(l16, k19, j21));
        }

        if (random.nextInt(32) == 0) {
            int k12 = x + random.nextInt(16) + 8;
            int i17 = random.nextInt(128);
            int l19 = z + random.nextInt(16) + 8;
            (new WorldGenPumpkin()).generate(world, random, new BlockPos(k12, i17, l19));
        }
        int l12 = 0;
        if (biome == Biomes.DESERT) {
            l12 += 10;
        }
        for (int j17 = 0; j17 < l12; j17++) {
            int i20 = x + random.nextInt(16) + 8;
            int k21 = random.nextInt(128);
            int k22 = z + random.nextInt(16) + 8;
            (new WorldGenCactus()).generate(world, random, new BlockPos(i20, k21, k22));
        }

        for (int k17 = 0; k17 < 50; k17++) {
            int j20 = x + random.nextInt(16) + 8;
            int l21 = random.nextInt(random.nextInt(120) + 8);
            int l22 = z + random.nextInt(16) + 8;
            (new WorldGenLiquids(Blocks.FLOWING_WATER)).generate(world, random, new BlockPos(j20, l21, l22));
        }

        for (int l17 = 0; l17 < 20; l17++) {
            int k20 = x + random.nextInt(16) + 8;
            int i22 = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8);
            int i23 = z + random.nextInt(16) + 8;
            (new WorldGenLiquids(Blocks.FLOWING_LAVA)).generate(world, random, new BlockPos(k20, i22, i23));
        }

        for (int x2 = 0; x2 < 16; x2++) {
            for (int z2 = 0; z2 < 16; z2++) {
                int y1 = world.getTopSolidOrLiquidBlock(new BlockPos(x2 + x + 8, 0, z2 + x + 8)).getY();
                double temp = biome.getTemperature() - ((double) (y1 - 64) / 64D) * 0.29999999999999999D;
                if (temp < 0.5D && y1 > 0 && y1 < 128 &&
                        world.isAirBlock(new BlockPos(x2 + x + 8, y1, z2 + x + 8)) &&
                        world.getBlockState(new BlockPos(x2 + x + 8, y1 - 1, z2 + x + 8)).getMaterial().isSolid() &&
                        world.getBlockState(new BlockPos(x2 + x + 8, y1 - 1, z2 + x + 8)).getMaterial() != Material.ICE) {
                    world.setBlockState(new BlockPos(x2 + x + 8, y1, z2 + x + 8), Blocks.SNOW.getDefaultState());
                }
            }

        }

        BlockSand.fallInstantly = false;
    }
}
