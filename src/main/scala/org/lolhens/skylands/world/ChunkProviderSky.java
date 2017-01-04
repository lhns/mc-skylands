package org.lolhens.skylands.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChunkProviderSky implements IChunkGenerator {
    private Random random;
    private NoiseGeneratorOctaves lperlinNoise1;
    private NoiseGeneratorOctaves lperlinNoise2;
    private NoiseGeneratorOctaves perlinNoise1;
    private NoiseGeneratorOctaves noise10;
    private NoiseGeneratorOctaves noise11;
    public NoiseGeneratorOctaves noiseGen5;
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves noise12;
    private World world;
    private double heightmapWIP[];
    private double pnr[];
    private double ar[];
    private double br[];
    private MapGenBase mapGenCaves = new MapGenCaves();
    private Biome[] biomes;
    double noise1[];
    double noisel1[];
    double noise2[];
    double noise5[];
    double noise6[];
    int field_28088_i[][];
    private double temperature[];

    public ChunkProviderSky(World world) {
        pnr = new double[256];
        ar = new double[256];
        br = new double[256];
        field_28088_i = new int[32][32];
        this.world = world;
        random = new Random(world.getSeed());
        lperlinNoise1 = new NoiseGeneratorOctaves(random, 16);
        lperlinNoise2 = new NoiseGeneratorOctaves(random, 16);
        perlinNoise1 = new NoiseGeneratorOctaves(random, 8);
        noise10 = new NoiseGeneratorOctaves(random, 4);
        noise11 = new NoiseGeneratorOctaves(random, 4);
        noiseGen5 = new NoiseGeneratorOctaves(random, 10);
        noiseGen6 = new NoiseGeneratorOctaves(random, 16);
        noise12 = new NoiseGeneratorOctaves(random, 8);
    }

    public void generateStone(int chunkX, int chunkZ, ChunkPrimer primer, double[] temperature, double[] humidity) {
        byte byte0 = 2;
        int k = byte0 + 1;
        byte byte1 = 33;
        int l = byte0 + 1;
        heightmapWIP = generateHeightmapWIP(heightmapWIP, chunkX * byte0, 0, chunkZ * byte0, k, byte1, l, temperature, humidity);
        for (int i1 = 0; i1 < byte0; i1++) {
            for (int j1 = 0; j1 < byte0; j1++) {
                for (int k1 = 0; k1 < 32; k1++) {
                    double d = 0.25D;
                    double d1 = heightmapWIP[((i1) * l + (j1)) * byte1 + (k1)];
                    double d2 = heightmapWIP[((i1) * l + (j1 + 1)) * byte1 + (k1)];
                    double d3 = heightmapWIP[((i1 + 1) * l + (j1)) * byte1 + (k1)];
                    double d4 = heightmapWIP[((i1 + 1) * l + (j1 + 1)) * byte1 + (k1)];
                    double d5 = (heightmapWIP[((i1) * l + (j1)) * byte1 + (k1 + 1)] - d1) * d;
                    double d6 = (heightmapWIP[((i1) * l + (j1 + 1)) * byte1 + (k1 + 1)] - d2) * d;
                    double d7 = (heightmapWIP[((i1 + 1) * l + (j1)) * byte1 + (k1 + 1)] - d3) * d;
                    double d8 = (heightmapWIP[((i1 + 1) * l + (j1 + 1)) * byte1 + (k1 + 1)] - d4) * d;
                    for (int l1 = 0; l1 < 4; l1++) {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        for (int i2 = 0; i2 < 8; i2++) {
                            int x = (j1 * 8) << 7;
                            int y = k1 * 4 + l1;
                            int z = (i2 + i1 * 8);
                            int coordIndex = z << 11 | x << 7 | y;
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            for (int k2 = 0; k2 < 8; k2++) {
                                IBlockState blockState = Blocks.AIR.getDefaultState();
                                if (d15 > 0.0D) {
                                    blockState = Blocks.STONE.getDefaultState();
                                }
                                primer.setBlockState(x, y, z, blockState);
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

    public void generateTerrain(int xChunk, int zChunk, ChunkPrimer primer, Biome biomes[]) {
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

    public Chunk prepareChunk(int i, int j) {
        return provideChunk(i, j);
    }

    public Chunk provideChunk(int chunkX, int chunkZ) {
        random.setSeed((long) chunkX * 0x4f9939f508L + (long) chunkZ * 0x1ef1565bd5L);
        ChunkPrimer primer = new ChunkPrimer();
        Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
        biomes = world.getWorldChunkManager().loadBlockGeneratorData(biomes, chunkX * 16, chunkZ * 16, 16, 16);
        double temperature[] = world.getWorldChunkManager().temperature;
        double humidity[] = world.getWorldChunkManager().humidity;
        generateStone(chunkX, chunkZ, primer, temperature, humidity);
        generateTerrain(chunkX, chunkZ, primer, biomes);
        mapGenCaves.generate(world, chunkX, chunkZ, primer);
        chunk.generateSkylightMap();

        return chunk;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }

    private double[] generateHeightmapWIP(double heightmapWIP[], int i, int j, int k, int l, int i1, int j1, double[] temperature, double[] humidity) {
        if (heightmapWIP == null) {
            heightmapWIP = new double[l * i1 * j1];
        }
        double d = 684.41200000000003D;
        double d1 = 684.41200000000003D;

        noise5 = noiseGen5.generateNoiseOctaves(noise5, i, k, l, j1, 1.121D, 1.121D, 0.5D);
        noise6 = noiseGen6.generateNoiseOctaves(noise6, i, k, l, j1, 200D, 200D, 0.5D);
        d *= 2D;
        noise1 = perlinNoise1.generateNoiseOctaves(noise1, i, j, k, l, i1, j1, d / 80D, d1 / 160D, d / 80D);
        noisel1 = lperlinNoise1.generateNoiseOctaves(noisel1, i, j, k, l, i1, j1, d, d1, d);
        noise2 = lperlinNoise2.generateNoiseOctaves(noise2, i, j, k, l, i1, j1, d, d1, d);
        int k1 = 0;
        for (int j2 = 0; j2 < l; j2++) {
            for (int l2 = 0; l2 < j1; l2++) {
                for (int j3 = 0; j3 < i1; j3++) {
                    double d10 = noisel1[k1] / 512D;
                    double d11 = noise2[k1] / 512D;
                    double d12 = (noise1[k1] / 10D + 1.0D) / 2D;

                    double d8;
                    if (d12 < 0.0D)
                        d8 = d10;
                    else if (d12 > 1.0D)
                        d8 = d11;
                    else
                        d8 = d10 + (d11 - d10) * d12;

                    d8 -= 8D;
                    int k3 = 32;
                    if (j3 > i1 - k3) {
                        double d13 = (float) (j3 - (i1 - k3)) / ((float) k3 - 1.0F);
                        d8 = d8 * (1.0D - d13) + -30D * d13;
                    }
                    k3 = 8;
                    if (j3 < k3) {
                        double d14 = (float) (k3 - j3) / ((float) k3 - 1.0F);
                        d8 = d8 * (1.0D - d14) + -30D * d14;
                    }
                    heightmapWIP[k1] = d8;
                    k1++;
                }

            }

        }

        return heightmapWIP;
    }

    public boolean chunkExists(int i, int j) {
        return true;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockSand.fallInstantly = true;
        int k = chunkX * 16;
        int l = chunkZ * 16;
        Biome biomegenbase = world.getBiome(new BlockPos(k + 16, 0, l + 16));
        random.setSeed(world.getSeed());
        long l1 = (random.nextLong() / 2L) * 2L + 1L;
        long l2 = (random.nextLong() / 2L) * 2L + 1L;
        random.setSeed((long) chunkX * l1 + (long) chunkZ * l2 ^ world.getSeed());
        double d = 0.25D;
        if (random.nextInt(4) == 0) {
            int i1 = k + random.nextInt(16) + 8;
            int l4 = random.nextInt(128);
            int i8 = l + random.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.WATER)).generate(world, random, new BlockPos(i1, l4, i8));
        }
        if (random.nextInt(8) == 0) {
            int j1 = k + random.nextInt(16) + 8;
            int i5 = random.nextInt(random.nextInt(120) + 8);
            int j8 = l + random.nextInt(16) + 8;
            if (i5 < 64 || random.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.LAVA)).generate(world, random, new BlockPos(j1, i5, j8));
            }
        }
        for (int k1 = 0; k1 < 8; k1++) {
            int j5 = k + random.nextInt(16) + 8;
            int k8 = random.nextInt(128);
            int i13 = l + random.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(world, random, new BlockPos(j5, k8, i13));
        }

        for (int i2 = 0; i2 < 10; i2++) {
            int k5 = k + random.nextInt(16);
            int l8 = random.nextInt(128);
            int j13 = l + random.nextInt(16);
            (new WorldGenClay(32)).generate(world, random, new BlockPos(k5, l8, j13));
        }

        for (int j2 = 0; j2 < 20; j2++) {
            int l5 = k + random.nextInt(16);
            int i9 = random.nextInt(128);
            int k13 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.DIRT.getDefaultState(), 32)).generate(world, random, new BlockPos(l5, i9, k13));
        }

        for (int k2 = 0; k2 < 10; k2++) {
            int i6 = k + random.nextInt(16);
            int j9 = random.nextInt(128);
            int l13 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), 32)).generate(world, random, new BlockPos(i6, j9, l13));
        }

        for (int i3 = 0; i3 < 20; i3++) {
            int j6 = k + random.nextInt(16);
            int k9 = random.nextInt(128);
            int i14 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), 16)).generate(world, random, new BlockPos(j6, k9, i14));
        }

        for (int j3 = 0; j3 < 20; j3++) {
            int k6 = k + random.nextInt(16);
            int l9 = random.nextInt(64);
            int j14 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), 8)).generate(world, random, new BlockPos(k6, l9, j14));
        }

        for (int k3 = 0; k3 < 2; k3++) {
            int l6 = k + random.nextInt(16);
            int i10 = random.nextInt(32);
            int k14 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), 8)).generate(world, random, new BlockPos(l6, i10, k14));
        }

        for (int l3 = 0; l3 < 8; l3++) {
            int i7 = k + random.nextInt(16);
            int j10 = random.nextInt(16);
            int l14 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), 7)).generate(world, random, new BlockPos(i7, j10, l14));
        }

        for (int i4 = 0; i4 < 1; i4++) {
            int j7 = k + random.nextInt(16);
            int k10 = random.nextInt(16);
            int i15 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), 7)).generate(world, random, new BlockPos(j7, k10, i15));
        }

        for (int j4 = 0; j4 < 1; j4++) {
            int k7 = k + random.nextInt(16);
            int l10 = random.nextInt(16) + random.nextInt(16);
            int j15 = l + random.nextInt(16);
            (new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), 6)).generate(world, random, new BlockPos(k7, l10, j15));
        }

        d = 0.5D;
        int k4 = (int) ((noise12.generateNoiseOctaves((double) k * d, (double) l * d) / 8D + random.nextDouble() * 4D + 4D) / 3D);

        int numTrees = 0;
        if (random.nextInt(10) == 0) numTrees++;
        if (biomegenbase == Biomes.FOREST) numTrees += k4 + 5;
        // rainforest
        if (biomegenbase == Biomes.FOREST_HILLS) numTrees += k4 + 5;
        // seasonalForest
        if (biomegenbase == Biomes.BIRCH_FOREST) numTrees += k4 + 2;
        if (biomegenbase == Biomes.TAIGA) numTrees += k4 + 5;
        if (biomegenbase == Biomes.DESERT) numTrees -= 20;
        // tundra
        if (biomegenbase == Biomes.COLD_TAIGA) numTrees -= 20;
        if (biomegenbase == Biomes.PLAINS) numTrees -= 20;
        for (int i11 = 0; i11 < numTrees; i11++) {
            int k15 = k + random.nextInt(16) + 8;
            int j18 = l + random.nextInt(16) + 8;
            WorldGenAbstractTree worldgenerator = biomegenbase.genBigTreeChance(random);
            //worldgenerator.func_517_a(1.0D, 1.0D, 1.0D);
            worldgenerator.generate(world, random, new BlockPos(k15, world.getHeightmapHeight(k15, j18), j18));
        }

        for (int j11 = 0; j11 < 2; j11++) {
            int l15 = k + random.nextInt(16) + 8;
            int k18 = random.nextInt(128);
            int i21 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION)).generate(world, random, new BlockPos(l15, k18, i21));
        }

        if (random.nextInt(2) == 0) {
            int x = k + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY)).generate(world, random, new BlockPos(x, y, z));
        }
        if (random.nextInt(4) == 0) {
            int l11 = k + random.nextInt(16) + 8;
            int j16 = random.nextInt(128);
            int i19 = l + random.nextInt(16) + 8;
            (new WorldGenBush(Blocks.BROWN_MUSHROOM)).generate(world, random, new BlockPos(l11, j16, i19)); // brown mushroom
        }
        if (random.nextInt(8) == 0) {
            int i12 = k + random.nextInt(16) + 8;
            int k16 = random.nextInt(128);
            int j19 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.RED_MUSHROOM, BlockFlower.EnumFlowerType.)).generate(world, random, i12, k16, j19);
        }
        for (int j12 = 0; j12 < 10; j12++) {
            int l16 = k + random.nextInt(16) + 8;
            int k19 = random.nextInt(128);
            int j21 = l + random.nextInt(16) + 8;
            (new WorldGenReed()).generate(world, random, new BlockPos(l16, k19, j21));
        }

        if (random.nextInt(32) == 0) {
            int k12 = k + random.nextInt(16) + 8;
            int i17 = random.nextInt(128);
            int l19 = l + random.nextInt(16) + 8;
            (new WorldGenPumpkin()).generate(world, random, new BlockPos(k12, i17, l19));
        }
        int l12 = 0;
        if (biomegenbase == Biomes.DESERT) {
            l12 += 10;
        }
        for (int j17 = 0; j17 < l12; j17++) {
            int i20 = k + random.nextInt(16) + 8;
            int k21 = random.nextInt(128);
            int k22 = l + random.nextInt(16) + 8;
            (new WorldGenCactus()).generate(world, random, new BlockPos(i20, k21, k22));
        }

        for (int k17 = 0; k17 < 50; k17++) {
            int j20 = k + random.nextInt(16) + 8;
            int l21 = random.nextInt(random.nextInt(120) + 8);
            int l22 = l + random.nextInt(16) + 8;
            (new WorldGenLiquids(Blocks.FLOWING_WATER)).generate(world, random, new BlockPos(j20, l21, l22));
        }

        for (int l17 = 0; l17 < 20; l17++) {
            int k20 = k + random.nextInt(16) + 8;
            int i22 = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8);
            int i23 = l + random.nextInt(16) + 8;
            (new WorldGenLiquids(Blocks.FLOWING_LAVA)).generate(world, random, new BlockPos(k20, i22, i23));
        }

        temperature = world.getBiomeProvider().getTemperatures(temperature, k + 8, l + 8, 16, 16);
        for (int x = k + 8; x < k + 8 + 16; x++) {
            for (int z = l + 8; z < l + 8 + 16; z++) {
                int j22 = x - (k + 8);
                int j23 = z - (l + 8);
                int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                double d1 = temperature[j22 * 16 + j23] - ((double) (y - 64) / 64D) * 0.29999999999999999D;
                if (d1 < 0.5D && y > 0 && y < 128 && world.isAirBlock(new BlockPos(x, y, z)) && world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial().isSolid() && world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial() != Material.ICE) {
                    world.setBlockState(new BlockPos(x, y, z), Blocks.SNOW.getDefaultState());
                }
            }

        }

        BlockSand.fallInstantly = false;
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        return true;
    }

    public boolean unload100OldestChunks() {
        return false;
    }

    public boolean canSave() {
        return true;
    }

    public String makeString() {
        return "RandomLevelSource";
    }
}
