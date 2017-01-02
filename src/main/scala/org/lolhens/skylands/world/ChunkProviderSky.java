package org.lolhens.skylands.world;
/*
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChunkProviderSky implements IChunkGenerator
{
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
    private double field_28080_q[];
    private double pnr[];
    private double ar[];
    private double br[];
    private MapGenBase mapGenCaves = new MapGenCaves();
    private BiomeGenBase field_28075_v[];
    double field_28093_d[];
    double field_28092_e[];
    double field_28091_f[];
    double field_28090_g[];
    double field_28089_h[];
    int field_28088_i[][];
    private double field_28074_w[];

    public ChunkProviderSky(World world)
    {
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

    public void func_28071_a(int i, int j, byte abyte0[])
    {
        byte byte0 = 2;
        int k = byte0 + 1;
        byte byte1 = 33;
        int l = byte0 + 1;
        field_28080_q = func_28073_a(field_28080_q, i * byte0, 0, j * byte0, k, byte1, l);
        for(int i1 = 0; i1 < byte0; i1++)
        {
            for(int j1 = 0; j1 < byte0; j1++)
            {
                for(int k1 = 0; k1 < 32; k1++)
                {
                    double d = 0.25D;
                    double d1 = field_28080_q[((i1 + 0) * l + (j1 + 0)) * byte1 + (k1 + 0)];
                    double d2 = field_28080_q[((i1 + 0) * l + (j1 + 1)) * byte1 + (k1 + 0)];
                    double d3 = field_28080_q[((i1 + 1) * l + (j1 + 0)) * byte1 + (k1 + 0)];
                    double d4 = field_28080_q[((i1 + 1) * l + (j1 + 1)) * byte1 + (k1 + 0)];
                    double d5 = (field_28080_q[((i1 + 0) * l + (j1 + 0)) * byte1 + (k1 + 1)] - d1) * d;
                    double d6 = (field_28080_q[((i1 + 0) * l + (j1 + 1)) * byte1 + (k1 + 1)] - d2) * d;
                    double d7 = (field_28080_q[((i1 + 1) * l + (j1 + 0)) * byte1 + (k1 + 1)] - d3) * d;
                    double d8 = (field_28080_q[((i1 + 1) * l + (j1 + 1)) * byte1 + (k1 + 1)] - d4) * d;
                    for(int l1 = 0; l1 < 4; l1++)
                    {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        for(int i2 = 0; i2 < 8; i2++)
                        {
                            int j2 = i2 + i1 * 8 << 11 | 0 + j1 * 8 << 7 | k1 * 4 + l1;
                            char c = '\200';
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            for(int k2 = 0; k2 < 8; k2++)
                            {
                                int l2 = 0;
                                if(d15 > 0.0D)
                                {
                                    l2 = Blocks.STONE.blockID;
                                }
                                abyte0[j2] = (byte)l2;
                                j2 += c;
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

    public void func_28072_a(int i, int j, byte abyte0[], BiomeGenBase abiomegenbase[])
    {
        double d = 0.03125D;
        pnr = noise10.generateNoiseOctaves(pnr, i * 16, j * 16, 0.0D, 16, 16, 1, d, d, 1.0D);
        ar = noise10.generateNoiseOctaves(ar, i * 16, 109.0134D, j * 16, 16, 1, 16, d, 1.0D, d);
        br = noise11.generateNoiseOctaves(br, i * 16, j * 16, 0.0D, 16, 16, 1, d * 2D, d * 2D, d * 2D);
        for(int k = 0; k < 16; k++)
        {
            for(int l = 0; l < 16; l++)
            {
                BiomeGenBase biomegenbase = abiomegenbase[k + l * 16];
                int i1 = (int)(br[k + l * 16] / 3D + 3D + random.nextDouble() * 0.25D);
                int j1 = -1;
                byte byte0 = biomegenbase.topBlock;
                byte byte1 = biomegenbase.fillerBlock;
                for(int k1 = 127; k1 >= 0; k1--)
                {
                    int l1 = (l * 16 + k) * 128 + k1;
                    byte byte2 = abyte0[l1];
                    if(byte2 == 0)
                    {
                        j1 = -1;
                        continue;
                    }
                    if(byte2 != Block.stone.blockID)
                    {
                        continue;
                    }
                    if(j1 == -1)
                    {
                        if(i1 <= 0)
                        {
                            byte0 = 0;
                            byte1 = (byte)Block.stone.blockID;
                        }
                        j1 = i1;
                        if(k1 >= 0)
                        {
                            abyte0[l1] = byte0;
                        } else
                        {
                            abyte0[l1] = byte1;
                        }
                        continue;
                    }
                    if(j1 <= 0)
                    {
                        continue;
                    }
                    j1--;
                    abyte0[l1] = byte1;
                    if(j1 == 0 && byte1 == Block.sand.blockID)
                    {
                        j1 = random.nextInt(4);
                        byte1 = (byte)Block.sandStone.blockID;
                    }
                }

            }

        }

    }

    public Chunk prepareChunk(int i, int j)
    {
        return provideChunk(i, j);
    }

    public Chunk provideChunk(int i, int j)
    {
        random.setSeed((long)i * 0x4f9939f508L + (long)j * 0x1ef1565bd5L);
        byte abyte0[] = new byte[32768];
        Chunk chunk = new Chunk(world, abyte0, i, j);
        field_28075_v = world.getWorldChunkManager().loadBlockGeneratorData(field_28075_v, i * 16, j * 16, 16, 16);
        double ad[] = world.getWorldChunkManager().temperature;
        func_28071_a(i, j, abyte0, field_28075_v, ad);
        func_28072_a(i, j, abyte0, field_28075_v);
        mapGenCaves.generate(world, i, j, abyte0);
        chunk.func_1024_c();
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

    private double[] func_28073_a(double ad[], int i, int j, int k, int l, int i1, int j1)
    {
        if(ad == null)
        {
            ad = new double[l * i1 * j1];
        }
        double d = 684.41200000000003D;
        double d1 = 684.41200000000003D;
        double ad1[] = world.getWorldChunkManager().temperature;
        double ad2[] = world.getWorldChunkManager().humidity;
        field_28090_g = noiseGen5.func_4109_a(field_28090_g, i, k, l, j1, 1.121D, 1.121D, 0.5D);
        field_28089_h = noiseGen6.func_4109_a(field_28089_h, i, k, l, j1, 200D, 200D, 0.5D);
        d *= 2D;
        field_28093_d = perlinNoise1.generateNoiseOctaves(field_28093_d, i, j, k, l, i1, j1, d / 80D, d1 / 160D, d / 80D);
        field_28092_e = lperlinNoise1.generateNoiseOctaves(field_28092_e, i, j, k, l, i1, j1, d, d1, d);
        field_28091_f = lperlinNoise2.generateNoiseOctaves(field_28091_f, i, j, k, l, i1, j1, d, d1, d);
        int k1 = 0;
        int l1 = 0;
        int i2 = 16 / l;
        for(int j2 = 0; j2 < l; j2++)
        {
            int k2 = j2 * i2 + i2 / 2;
            for(int l2 = 0; l2 < j1; l2++)
            {
                int i3 = l2 * i2 + i2 / 2;
                double d2 = ad1[k2 * 16 + i3];
                double d3 = ad2[k2 * 16 + i3] * d2;
                double d4 = 1.0D - d3;
                d4 *= d4;
                d4 *= d4;
                d4 = 1.0D - d4;
                double d5 = (field_28090_g[l1] + 256D) / 512D;
                d5 *= d4;
                if(d5 > 1.0D)
                {
                    d5 = 1.0D;
                }
                double d6 = field_28089_h[l1] / 8000D;
                if(d6 < 0.0D)
                {
                    d6 = -d6 * 0.29999999999999999D;
                }
                d6 = d6 * 3D - 2D;
                if(d6 > 1.0D)
                {
                    d6 = 1.0D;
                }
                d6 /= 8D;
                d6 = 0.0D;
                if(d5 < 0.0D)
                {
                    d5 = 0.0D;
                }
                d5 += 0.5D;
                d6 = (d6 * (double)i1) / 16D;
                l1++;
                double d7 = (double)i1 / 2D;
                for(int j3 = 0; j3 < i1; j3++)
                {
                    double d8 = 0.0D;
                    double d9 = (((double)j3 - d7) * 8D) / d5;
                    if(d9 < 0.0D)
                    {
                        d9 *= -1D;
                    }
                    double d10 = field_28092_e[k1] / 512D;
                    double d11 = field_28091_f[k1] / 512D;
                    double d12 = (field_28093_d[k1] / 10D + 1.0D) / 2D;
                    if(d12 < 0.0D)
                    {
                        d8 = d10;
                    } else
                    if(d12 > 1.0D)
                    {
                        d8 = d11;
                    } else
                    {
                        d8 = d10 + (d11 - d10) * d12;
                    }
                    d8 -= 8D;
                    int k3 = 32;
                    if(j3 > i1 - k3)
                    {
                        double d13 = (float)(j3 - (i1 - k3)) / ((float)k3 - 1.0F);
                        d8 = d8 * (1.0D - d13) + -30D * d13;
                    }
                    k3 = 8;
                    if(j3 < k3)
                    {
                        double d14 = (float)(k3 - j3) / ((float)k3 - 1.0F);
                        d8 = d8 * (1.0D - d14) + -30D * d14;
                    }
                    ad[k1] = d8;
                    k1++;
                }

            }

        }

        return ad;
    }

    public boolean chunkExists(int i, int j)
    {
        return true;
    }

    @Override
    public void populate(int x, int z) {
        BlockSand.fallInstantly = true;
        int k = x * 16;
        int l = z * 16;
        BiomeGenBase biomegenbase = world.getWorldChunkManager().getBiomeGenAt(k + 16, l + 16);
        random.setSeed(world.getRandomSeed());
        long l1 = (random.nextLong() / 2L) * 2L + 1L;
        long l2 = (random.nextLong() / 2L) * 2L + 1L;
        random.setSeed((long)x * l1 + (long)z * l2 ^ world.getRandomSeed());
        double d = 0.25D;
        if(random.nextInt(4) == 0)
        {
            int i1 = k + random.nextInt(16) + 8;
            int l4 = random.nextInt(128);
            int i8 = l + random.nextInt(16) + 8;
            (new WorldGenLakes(Block.waterStill.blockID)).generate(world, random, i1, l4, i8);
        }
        if(random.nextInt(8) == 0)
        {
            int j1 = k + random.nextInt(16) + 8;
            int i5 = random.nextInt(random.nextInt(120) + 8);
            int j8 = l + random.nextInt(16) + 8;
            if(i5 < 64 || random.nextInt(10) == 0)
            {
                (new WorldGenLakes(Block.lavaStill.blockID)).generate(world, random, j1, i5, j8);
            }
        }
        for(int k1 = 0; k1 < 8; k1++)
        {
            int j5 = k + random.nextInt(16) + 8;
            int k8 = random.nextInt(128);
            int i13 = l + random.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(world, random, j5, k8, i13);
        }

        for(int i2 = 0; i2 < 10; i2++)
        {
            int k5 = k + random.nextInt(16);
            int l8 = random.nextInt(128);
            int j13 = l + random.nextInt(16);
            (new WorldGenClay(32)).generate(world, random, k5, l8, j13);
        }

        for(int j2 = 0; j2 < 20; j2++)
        {
            int l5 = k + random.nextInt(16);
            int i9 = random.nextInt(128);
            int k13 = l + random.nextInt(16);
            (new WorldGenMinable(Block.dirt.blockID, 32)).generate(world, random, l5, i9, k13);
        }

        for(int k2 = 0; k2 < 10; k2++)
        {
            int i6 = k + random.nextInt(16);
            int j9 = random.nextInt(128);
            int l13 = l + random.nextInt(16);
            (new WorldGenMinable(Block.gravel.blockID, 32)).generate(world, random, i6, j9, l13);
        }

        for(int i3 = 0; i3 < 20; i3++)
        {
            int j6 = k + random.nextInt(16);
            int k9 = random.nextInt(128);
            int i14 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreCoal.blockID, 16)).generate(world, random, j6, k9, i14);
        }

        for(int j3 = 0; j3 < 20; j3++)
        {
            int k6 = k + random.nextInt(16);
            int l9 = random.nextInt(64);
            int j14 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreIron.blockID, 8)).generate(world, random, k6, l9, j14);
        }

        for(int k3 = 0; k3 < 2; k3++)
        {
            int l6 = k + random.nextInt(16);
            int i10 = random.nextInt(32);
            int k14 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreGold.blockID, 8)).generate(world, random, l6, i10, k14);
        }

        for(int l3 = 0; l3 < 8; l3++)
        {
            int i7 = k + random.nextInt(16);
            int j10 = random.nextInt(16);
            int l14 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreRedstone.blockID, 7)).generate(world, random, i7, j10, l14);
        }

        for(int i4 = 0; i4 < 1; i4++)
        {
            int j7 = k + random.nextInt(16);
            int k10 = random.nextInt(16);
            int i15 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreDiamond.blockID, 7)).generate(world, random, j7, k10, i15);
        }

        for(int j4 = 0; j4 < 1; j4++)
        {
            int k7 = k + random.nextInt(16);
            int l10 = random.nextInt(16) + random.nextInt(16);
            int j15 = l + random.nextInt(16);
            (new WorldGenMinable(Block.oreLapis.blockID, 6)).generate(world, random, k7, l10, j15);
        }

        d = 0.5D;
        int k4 = (int)((noise12.func_806_a((double)k * d, (double)l * d) / 8D + random.nextDouble() * 4D + 4D) / 3D);
        int l7 = 0;
        if(random.nextInt(10) == 0)
        {
            l7++;
        }
        if(biomegenbase == BiomeGenBase.forest)
        {
            l7 += k4 + 5;
        }
        if(biomegenbase == BiomeGenBase.rainforest)
        {
            l7 += k4 + 5;
        }
        if(biomegenbase == BiomeGenBase.seasonalForest)
        {
            l7 += k4 + 2;
        }
        if(biomegenbase == BiomeGenBase.taiga)
        {
            l7 += k4 + 5;
        }
        if(biomegenbase == BiomeGenBase.desert)
        {
            l7 -= 20;
        }
        if(biomegenbase == BiomeGenBase.tundra)
        {
            l7 -= 20;
        }
        if(biomegenbase == BiomeGenBase.plains)
        {
            l7 -= 20;
        }
        for(int i11 = 0; i11 < l7; i11++)
        {
            int k15 = k + random.nextInt(16) + 8;
            int j18 = l + random.nextInt(16) + 8;
            WorldGenerator worldgenerator = biomegenbase.getRandomWorldGenForTrees(random);
            worldgenerator.func_517_a(1.0D, 1.0D, 1.0D);
            worldgenerator.generate(world, random, k15, world.getHeightValue(k15, j18), j18);
        }

        for(int j11 = 0; j11 < 2; j11++)
        {
            int l15 = k + random.nextInt(16) + 8;
            int k18 = random.nextInt(128);
            int i21 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Block.plantYellow.blockID)).generate(world, random, l15, k18, i21);
        }

        if(random.nextInt(2) == 0)
        {
            int k11 = k + random.nextInt(16) + 8;
            int i16 = random.nextInt(128);
            int l18 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Block.plantRed.blockID)).generate(world, random, k11, i16, l18);
        }
        if(random.nextInt(4) == 0)
        {
            int l11 = k + random.nextInt(16) + 8;
            int j16 = random.nextInt(128);
            int i19 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Block.mushroomBrown.blockID)).generate(world, random, l11, j16, i19);
        }
        if(random.nextInt(8) == 0)
        {
            int i12 = k + random.nextInt(16) + 8;
            int k16 = random.nextInt(128);
            int j19 = l + random.nextInt(16) + 8;
            (new WorldGenFlowers(Blocks.RED_MUSHROOM, BlockFlower.EnumFlowerType.)).generate(world, random, i12, k16, j19);
        }
        for(int j12 = 0; j12 < 10; j12++)
        {
            int l16 = k + random.nextInt(16) + 8;
            int k19 = random.nextInt(128);
            int j21 = l + random.nextInt(16) + 8;
            (new WorldGenReed()).generate(world, random, l16, k19, j21);
        }

        if(random.nextInt(32) == 0)
        {
            int k12 = k + random.nextInt(16) + 8;
            int i17 = random.nextInt(128);
            int l19 = l + random.nextInt(16) + 8;
            (new WorldGenPumpkin()).generate(world, random, new BlockPos(k12, i17, l19));
        }
        int l12 = 0;
        if(biomegenbase == BiomeGenBase.desert)
        {
            l12 += 10;
        }
        for(int j17 = 0; j17 < l12; j17++)
        {
            int i20 = k + random.nextInt(16) + 8;
            int k21 = random.nextInt(128);
            int k22 = l + random.nextInt(16) + 8;
            (new WorldGenCactus()).generate(world, random, i20, k21, k22);
        }

        for(int k17 = 0; k17 < 50; k17++)
        {
            int j20 = k + random.nextInt(16) + 8;
            int l21 = random.nextInt(random.nextInt(120) + 8);
            int l22 = l + random.nextInt(16) + 8;
            (new WorldGenLiquids(Block.waterMoving.blockID)).generate(world, random, j20, l21, l22);
        }

        for(int l17 = 0; l17 < 20; l17++)
        {
            int k20 = k + random.nextInt(16) + 8;
            int i22 = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8);
            int i23 = l + random.nextInt(16) + 8;
            (new WorldGenLiquids(Block.lavaMoving.blockID)).generate(world, random, k20, i22, i23);
        }

        field_28074_w = world.getWorldChunkManager().getTemperatures(field_28074_w, k + 8, l + 8, 16, 16);
        for(int i18 = k + 8; i18 < k + 8 + 16; i18++)
        {
            for(int l20 = l + 8; l20 < l + 8 + 16; l20++)
            {
                int j22 = i18 - (k + 8);
                int j23 = l20 - (l + 8);
                int k23 = world.findTopSolidBlock(i18, l20);
                double d1 = field_28074_w[j22 * 16 + j23] - ((double)(k23 - 64) / 64D) * 0.29999999999999999D;
                if(d1 < 0.5D && k23 > 0 && k23 < 128 && world.isAirBlock(i18, k23, l20) && world.getBlockMaterial(i18, k23 - 1, l20).getIsSolid() && world.getBlockMaterial(i18, k23 - 1, l20) != Material.ice)
                {
                    world.setBlockWithNotify(i18, k23, l20, Block.snow.blockID);
                }
            }

        }

        BlockSand.fallInstantly = false;
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
    {
        return true;
    }

    public boolean unload100OldestChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return true;
    }

    public String makeString()
    {
        return "RandomLevelSource";
    }
}
*/