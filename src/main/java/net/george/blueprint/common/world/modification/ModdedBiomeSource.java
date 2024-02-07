package net.george.blueprint.common.world.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.george.blueprint.core.registry.BlueprintBiomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.SeedMixer;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.*;
import java.util.stream.Stream;

/**
 * A {@link BiomeSource} subclass that wraps another {@link BiomeSource} instance and overlays its biomes with sliced modded biome providers.
 *
 * @author SmellyModder (Luke Tonon)
 * @see ModdedBiomeSlice
 */
public final class ModdedBiomeSource extends BiomeSource {
    public static final Codec<BiomeSource> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BiomeSource.CODEC.fieldOf("original_biome_source").forGetter(thisBiomeSource -> thisBiomeSource instanceof ModdedBiomeSource moddedBiomeSource ? moddedBiomeSource.originalSource : thisBiomeSource)
    ).apply(instance, biomeSource -> biomeSource));
    private final Registry<Biome> biomes;
    private final BiomeSource originalSource;
    private final ThreadLocal<SlicesCache> slicesCache = ThreadLocal.withInitial(SlicesCache::new);
    private final ModdedBiomeSlice[] slices;
    private final int totalWeight;
    private final int size;
    private final Biome originalSourceMarker;
    private final long slicesSeed;
    private final long slicesZoomSeed;
    private final long obfuscatedSeed;

    public ModdedBiomeSource(Registry<Biome> biomes, BiomeSource originalSource, ArrayList<ModdedBiomeSlice> slices, int size, long seed, long dimensionSeedModifier) {
        this(biomes, originalSource, slices, size + MathHelper.ceil(Math.log(slices.size()) / Math.log(2)), seed, seed + 1791510900 + dimensionSeedModifier, seed - 771160217 + dimensionSeedModifier);
    }

    public ModdedBiomeSource(Registry<Biome> biomes, BiomeSource originalSource, ArrayList<ModdedBiomeSlice> slices, int size, long seed, long slicesSeed, long slicesZoomSeed) {
        super(new ArrayList<>(combinePossibleBiomes(originalSource.getBiomes(), slices, biomes)));
        this.biomes = biomes;
        this.originalSource = originalSource;
        this.slices = slices.toArray(new ModdedBiomeSlice[0]);
        this.totalWeight = Stream.of(this.slices).map(ModdedBiomeSlice::weight).reduce(0, Integer::sum);
        this.size = size;
        this.originalSourceMarker = biomes.getOrThrow(BlueprintBiomes.ORIGINAL_SOURCE_MARKER.getKey());
        this.slicesSeed = slicesSeed;
        this.slicesZoomSeed = slicesZoomSeed;
        this.obfuscatedSeed = BiomeAccess.hashSeed(seed);
    }

    private static Set<RegistryEntry<Biome>> combinePossibleBiomes(Set<RegistryEntry<Biome>> possibleBiomes, ArrayList<ModdedBiomeSlice> slices, Registry<Biome> registry) {
        Set<RegistryEntry<Biome>> biomes = new HashSet<>(possibleBiomes);
        for (ModdedBiomeSlice slice : slices) {
            biomes.addAll(slice.provider().getAdditionalPossibleBiomes(registry));
        }
        return biomes;
    }

    @Override
    public void addDebugInfo(List<String> strings, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler sampler) {
        BiomeSource original = this.originalSource;
        original.addDebugInfo(strings, pos, sampler);
        if (!(original instanceof ModdedBiomeSource))
            strings.add("Modded Biome Slice: " + this.getSlice(BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getZ())).name());
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return new ModdedBiomeSource(this.biomes, this.originalSource, new ArrayList<>(List.of(this.slices)), this.size, seed, this.slicesSeed, this.slicesZoomSeed);
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        RegistryEntry<Biome> biome = this.getSlice(x, z).provider().getNoiseBiome(x, y, z, noise, this.originalSource, this.biomes);
        return biome.value() == this.originalSourceMarker ? this.originalSource.getBiome(x, y, z, noise) : biome;
    }

    private ModdedBiomeSlice getSlice(int x, int z) {
        return this.slicesCache.get().getSlice(this, x, z);
    }

    private ModdedBiomeSlice getSliceUncached(int x, int z) {
        int cordX = BiomeCoords.toBlock(x);
        int cordZ = BiomeCoords.toBlock(z);
        long slicesZoomSeed = this.slicesZoomSeed;
        //Randomly zooms the x and z coordinates by cutting them into cells and adding some randomness for each zoom
        for (int i = 0; i < this.size; i++) {
            int cellPosX = cordX & 1;
            int cellPosZ = cordZ & 1;
            int cellX = cordX >> 1;
            int cellZ = cordZ >> 1;
            if (cellPosX == 0 && cellPosZ == 0) {
                cordX = cellX;
                cordZ = cellZ;
            } else if (cellPosX == 0) {
                if (nextInt(slicesZoomSeed, cellX << 1, cellZ << 1, 2) == 0) {
                    cordZ = cellZ;
                } else {
                    cordZ = (cordZ + 1) >> 1;
                }
                cordX = cellX;
            } else if (cellPosZ == 0) {
                if (nextInt(slicesZoomSeed, cellX << 1, cellZ << 1, 2) == 0) {
                    cordX = cellX;
                } else {
                    cordX = (cordX + 1) >> 1;
                }
                cordZ = cellZ;
            } else {
                int offsetChoice = nextInt(slicesZoomSeed, cellX << 1, cellZ << 1, 4);
                if (offsetChoice == 0) {
                    cordX = cellX;
                    cordZ = cellZ;
                } else if (offsetChoice == 1) {
                    cordX = (cordX + 1) >> 1;
                    cordZ = cellZ;
                } else if (offsetChoice == 2) {
                    cordX = cellX;
                    cordZ = (cordZ + 1) >> 1;
                } else {
                    cordX = (cordX + 1) >> 1;
                    cordZ = (cordZ + 1) >> 1;
                }
            }
        }
        //After transforming the x and z coordinates to be in a randomized cell, we generate the pseudorandom weight associated with the transformed coordinates
        int randomWeight = nextInt(this.slicesSeed, cordX, cordZ, this.totalWeight);
        for (ModdedBiomeSlice slice : this.slices) {
            if ((randomWeight -= slice.weight()) < 0) return slice;
        }
        return this.slices[0];
    }

    /**
     * Gets the {@link ModdedBiomeSlice} instance at given x, y, and z coordinates after it has been zoomed by vanilla's {@link BiomeAccess}.
     * <p>This method is used internally by {@link net.george.blueprint.core.registry.BlueprintSurfaceRules.ModdednessSliceConditionSource}.</p>
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The {@link ModdedBiomeSlice} instance at given x, y, and z coordinates after it has been zoomed by vanilla's {@link BiomeAccess}.
     */
    //Vanilla applies a zoom when getting noise biomes, and we must account for this in ModdednessSliceConditionSource
    public ModdedBiomeSlice getSliceWithVanillaZoom(int x, int y, int z) {
        int i = x - 2;
        int j = y - 2;
        int k = z - 2;
        int l = i >> 2;
        int i1 = j >> 2;
        int j1 = k >> 2;
        double d0 = (double) (i & 3) / 4.0D;
        double d1 = (double) (j & 3) / 4.0D;
        double d2 = (double) (k & 3) / 4.0D;
        int k1 = 0;
        double d3 = Double.POSITIVE_INFINITY;

        for (int l1 = 0; l1 < 8; ++l1) {
            boolean flag = (l1 & 4) == 0;
            boolean flag1 = (l1 & 2) == 0;
            boolean flag2 = (l1 & 1) == 0;
            int i2 = flag ? l : l + 1;
            int j2 = flag1 ? i1 : i1 + 1;
            int k2 = flag2 ? j1 : j1 + 1;
            double d4 = flag ? d0 : d0 - 1.0D;
            double d5 = flag1 ? d1 : d1 - 1.0D;
            double d6 = flag2 ? d2 : d2 - 1.0D;
            double d7 = getFiddledDistance(this.obfuscatedSeed, i2, j2, k2, d4, d5, d6);
            if (d3 > d7) {
                k1 = l1;
                d3 = d7;
            }
        }

        int l2 = (k1 & 4) == 0 ? l : l + 1;
        int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;
        return this.getSlice(l2, j3);
    }

    private static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
        long m = SeedMixer.mixSeed(l, i);
        m = SeedMixer.mixSeed(m, j);
        m = SeedMixer.mixSeed(m, k);
        m = SeedMixer.mixSeed(m, i);
        m = SeedMixer.mixSeed(m, j);
        m = SeedMixer.mixSeed(m, k);
        double g = getFiddle(m);
        m = SeedMixer.mixSeed(m, l);
        double h = getFiddle(m);
        m = SeedMixer.mixSeed(m, l);
        double n = getFiddle(m);
        return MathHelper.square(f + n) + MathHelper.square(e + h) + MathHelper.square(d + g);
    }

    private static double getFiddle(long l) {
        double d0 = (double) Math.floorMod(l >> 24, 1024) / 1024.0D;
        return (d0 - 0.5D) * 0.9D;
    }

    private static int nextInt(long seed, int x, int z, int bound) {
        long next = SeedMixer.mixSeed(seed, x);
        next = SeedMixer.mixSeed(next, z);
        next = SeedMixer.mixSeed(next, x);
        return Math.floorMod(SeedMixer.mixSeed(next, z) >> 24, bound);
    }

    //The y-axis doesn't matter for selecting slices, so we can cache our slices on the xz plane to greatly boost performance.
    private static class SlicesCache {
        private final long[] lastXZHashes;
        private final ModdedBiomeSlice[] slices;

        private SlicesCache() {
            Arrays.fill(this.lastXZHashes = new long[256], -9223372036854775807L);
            this.slices = new ModdedBiomeSlice[256];
        }

        private ModdedBiomeSlice getSlice(ModdedBiomeSource biomeSource, int x, int z) {
            int xIndex = ChunkSectionPos.getLocalCoord(x);
            int zIndex = ChunkSectionPos.getLocalCoord(z);
            int index = 16 * xIndex + zIndex;
            long xzHash = ChunkPos.toLong(x, z);
            if (this.lastXZHashes[index] != xzHash) {
                this.lastXZHashes[index] = xzHash;
                return this.slices[index] = biomeSource.getSliceUncached(x, z);
            }
            return this.slices[index];
        }
    }
}
