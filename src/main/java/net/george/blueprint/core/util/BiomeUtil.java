package net.george.blueprint.core.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.george.blueprint.common.world.modification.ModdedBiomeSlice;
import net.george.blueprint.common.world.modification.ModdedBiomeSource;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.registry.BasicRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class for biomes.
 *
 * @author bageldotjpg
 * @author SmellyModder (Luke Tonon)
 * @author ExpensiveKoala
 * @author Mr.George
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class BiomeUtil {
    private static final Set<RegistryKey<Biome>> CUSTOM_END_MUSIC_BIOMES = new HashSet<>();
    private static final BasicRegistry<Codec<? extends ModdedBiomeProvider>> MODDED_PROVIDERS = new BasicRegistry<>();

    static {
        MODDED_PROVIDERS.register(new Identifier(Blueprint.MOD_ID, "original"), OriginalModdedBiomeProvider.CODEC);
        MODDED_PROVIDERS.register(new Identifier(Blueprint.MOD_ID, "multi_noise"), MultiNoiseModdedBiomeProvider.CODEC);
        MODDED_PROVIDERS.register(new Identifier(Blueprint.MOD_ID, "overlay"), OverlayModdedBiomeProvider.CODEC);
        MODDED_PROVIDERS.register(new Identifier(Blueprint.MOD_ID, "biome_source"), BiomeSourceModdedBiomeProvider.CODEC);
    }

    /**
     * Registers a new {@link ModdedBiomeProvider} type that can be serialized and deserialized.
     *
     * @param name  A {@link Identifier} name for the provider.
     * @param codec A {@link Codec} to use for serializing and deserializing instances of the {@link ModdedBiomeProvider} type.
     */
    public static synchronized void registerBiomeProvider(Identifier name, Codec<? extends ModdedBiomeProvider> codec) {
        MODDED_PROVIDERS.register(name, codec);
    }

    /**
     * Marks the {@link RegistryKey} belonging to a {@link Biome} to have it play its music in the end.
     * <p>The music for biomes in the end is hardcoded, and this gets around that.</p>
     * <p>This method is safe to call during parallel mod loading.</p>
     *
     * @param biomeName The {@link RegistryKey} belonging to a {@link Biome} to have it play its music in the end.
     */
    public static synchronized void markEndBiomeCustomMusic(RegistryKey<Biome> biomeName) {
        CUSTOM_END_MUSIC_BIOMES.add(biomeName);
    }

    /**
     * Checks if a {@link RegistryKey} belonging to a {@link Biome} should have the {@link Biome} plays its custom music in the end.
     *
     * @param biomeName The {@link RegistryKey} belonging to a {@link Biome} to check.
     * @return If a {@link RegistryKey} belonging to a {@link Biome} should have the {@link Biome} plays its custom music in the end.
     */
    public static boolean shouldPlayCustomEndMusic(RegistryKey<Biome> biomeName) {
        return CUSTOM_END_MUSIC_BIOMES.contains(biomeName);
    }

    /**
     * Get the {@link Biome} id given a {@link Biome} {@link RegistryKey}.
     *
     * @param biome The {@link Biome} {@link RegistryKey} to get the id of.
     * @return The id of the provided {@link Biome} {@link RegistryKey}.
     */
    public static int getId(@Nonnull RegistryKey<Biome> biome) {
        return BuiltinRegistries.BIOME.getRawId(BuiltinRegistries.BIOME.get(biome));
    }

    /**
     * The interface used for selecting biomes in {@link ModdedBiomeSlice} instances.
     * <p>Use {@link #CODEC} for serializing and deserializing instances of this class.</p>
     *
     * @author SmellyModder (Luke Tonon)
     * @see ModdedBiomeSource
     */
    public interface ModdedBiomeProvider {
        Codec<ModdedBiomeProvider> CODEC = BiomeUtil.MODDED_PROVIDERS.dispatchStable(ModdedBiomeProvider::codec, Function.identity());

        /**
         * Gets a holder of a noise {@link Biome} at a position in a modded slice.
         *
         * @param x        The x pos, shifted by {@link BiomeCoords#fromBlock(int)}.
         * @param y        The y pos, shifted by {@link BiomeCoords#fromBlock(int)}.
         * @param z        The z pos, shifted by {@link BiomeCoords#fromBlock(int)}.
         * @param sampler  A {@link MultiNoiseUtil.MultiNoiseSampler} instance to sample {@link MultiNoiseUtil.NoiseValuePoint} instances.
         * @param original The original {@link BiomeSource} instance that this provider is modding.
         * @param registry The biome {@link Registry} instance to use if needed.
         * @return A noise {@link Biome} at a position in a modded slice.
         */

        RegistryEntry<Biome> getNoiseBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, BiomeSource original, Registry<Biome> registry);

        /**
         * Gets a set of the additional possible biomes that this provider may have.
         *
         * @param registry The biome {@link Registry} instance to use if needed.
         * @return A set of the additional possible biomes that this provider may have.
         */
        Set<RegistryEntry<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry);

        /**
         * Gets a {@link Codec} instance for serializing and deserializing this provider.
         *
         * @return A {@link Codec} instance for serializing and deserializing this provider.
         */
        Codec<? extends ModdedBiomeProvider> codec();
    }

    /**
     * A simple {@link ModdedBiomeProvider} implementation that uses the original biome source's {@link BiomeSource#getBiome(int, int, int, MultiNoiseUtil.MultiNoiseSampler)} method.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public record OriginalModdedBiomeProvider() implements ModdedBiomeProvider {
        public static final Codec<OriginalModdedBiomeProvider> CODEC = Codec.unit(new OriginalModdedBiomeProvider());

        @Override
        public RegistryEntry<Biome> getNoiseBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, BiomeSource original, Registry<Biome> registry) {
            return original.getBiome(x, y, z, sampler);
        }

        @Override
        public Codec<? extends ModdedBiomeProvider> codec() {
            return CODEC;
        }

        @Override
        public Set<RegistryEntry<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            return new HashSet<>(0);
        }
    }

    /**
     * A {@link ModdedBiomeProvider} implementation that uses a {@link MultiNoiseUtil.NoiseHypercube} instance for selecting its biomes.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public record MultiNoiseModdedBiomeProvider(MultiNoiseUtil.Entries<RegistryKey<Biome>> biomes) implements ModdedBiomeProvider {
        public static final Codec<MultiNoiseModdedBiomeProvider> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codecs.nonEmptyList(RecordCodecBuilder.<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>>create((pairInstance) ->
                        pairInstance.group(MultiNoiseUtil.NoiseHypercube.CODEC.fieldOf("parameters")
                                .forGetter(Pair::getFirst), RegistryKey.createCodec(Registry.BIOME_KEY)
                                .fieldOf("biome").forGetter(Pair::getSecond)).apply(pairInstance, Pair::of)).listOf()).xmap(MultiNoiseUtil.Entries::new, MultiNoiseUtil.Entries::getEntries).fieldOf("biomes").forGetter(sampler -> sampler.biomes)
        ).apply(instance, MultiNoiseModdedBiomeProvider::new));

        @Override
        public RegistryEntry<Biome> getNoiseBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, BiomeSource original, Registry<Biome> registry) {
            return registry.entryOf(this.biomes.method_39529(sampler.sample(x, y, z)));
        }

        @Override
        public Codec<? extends ModdedBiomeProvider> codec() {
            return CODEC;
        }

        @Override
        public Set<RegistryEntry<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            return this.biomes.getEntries().stream().map(pair -> registry.entryOf(pair.getSecond())).collect(Collectors.toSet());
        }
    }

    /**
     * A {@link ModdedBiomeProvider} implementation that maps out {@link BiomeSource} instances for overlaying specific biomes.
     * <p>This is especially useful for sub-biomes.</p>
     *
     * @author SmellyModder (Luke Tonon)
     */
    public record OverlayModdedBiomeProvider(List<Pair<RegistryEntryList<Biome>, BiomeSource>> overlays) implements ModdedBiomeProvider {
        public static final Codec<OverlayModdedBiomeProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.mapPair(RegistryCodecs.entryList(Registry.BIOME_KEY).fieldOf("matches_biomes"),
                        BiomeSource.CODEC.fieldOf("biome_source")).codec().listOf().fieldOf("overlays").forGetter(provider -> provider.overlays)
        ).apply(instance, OverlayModdedBiomeProvider::new));

        @Override
        public RegistryEntry<Biome> getNoiseBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, BiomeSource original, Registry<Biome> registry) {
            RegistryEntry<Biome> originalBiome = original.getBiome(x, y, z, sampler);
            for (var overlay : this.overlays) {
                if (overlay.getFirst().contains(originalBiome)) return overlay.getSecond().getBiome(x, y, z, sampler);
            }
            return originalBiome;
        }

        @Override
        public Set<RegistryEntry<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            HashSet<RegistryEntry<Biome>> biomes = new HashSet<>();
            this.overlays.forEach(overlay -> biomes.addAll(overlay.getSecond().getBiomes()));
            return biomes;
        }

        @Override
        public Codec<? extends ModdedBiomeProvider> codec() {
            return CODEC;
        }
    }

    /**
     * A {@link ModdedBiomeProvider} implementation that uses a {@link BiomeSource} instance for selecting its biomes.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public record BiomeSourceModdedBiomeProvider(BiomeSource biomeSource) implements ModdedBiomeProvider {
        public static final Codec<BiomeSourceModdedBiomeProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(provider -> provider.biomeSource)
        ).apply(instance, BiomeSourceModdedBiomeProvider::new));

        @Override
        public RegistryEntry<Biome> getNoiseBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, BiomeSource original, Registry<Biome> registry) {
            return this.biomeSource.getBiome(x, y, z, sampler);
        }

        @Override
        public Set<RegistryEntry<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            return this.biomeSource.getBiomes();
        }

        @Override
        public Codec<? extends ModdedBiomeProvider> codec() {
            return CODEC;
        }
    }
}
