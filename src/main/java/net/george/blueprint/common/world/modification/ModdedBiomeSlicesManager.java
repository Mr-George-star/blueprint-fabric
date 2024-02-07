package net.george.blueprint.common.world.modification;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.BlueprintConfig;
import net.george.blueprint.core.events.ResourceReloadCallback;
import net.george.blueprint.core.util.DataUtil;
import net.george.blueprint.core.util.modification.selection.ConditionedResourceSelector;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.CheckerboardBiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

import java.util.*;

/**
 * The data manager class for Blueprint's modded biome sources system.
 * <p>This class handles the deserializing and applying of {@link ModdedBiomeSlice} instances.</p>
 *
 * @author SmellyModder (Luke Tonon) || Recreated by: Mr.George
 */
public final class ModdedBiomeSlicesManager extends JsonDataLoader {
    private static ModdedBiomeSlicesManager INSTANCE;
    private final List<Pair<ConditionedResourceSelector, ModdedBiomeSlice>> unassignedSlices = new LinkedList<>();
    private final RegistryOps<JsonElement> registryOps;

    public ModdedBiomeSlicesManager(RegistryOps<JsonElement> registryOps) {
        super(new Gson(), "modded_biome_slices");
        this.registryOps = registryOps;
        this.registerEvents();
    }

    public void registerEvents() {
        ResourceReloadCallback.EVENT.register((contents, reloaders) -> {
            try {
                reloaders.add(INSTANCE = new ModdedBiomeSlicesManager(DataUtil.createRegistryOps(contents)));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return reloaders;
        });
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            if (INSTANCE == null) {
                return;
            }
            List<Pair<ConditionedResourceSelector, ModdedBiomeSlice>> unassignedSlices = INSTANCE.unassignedSlices;
            if (unassignedSlices.isEmpty()) {
                return;
            }
            GeneratorOptions worldGenSettings = server.getSaveProperties().getGeneratorOptions();
            Registry<DimensionOptions> dimensions = worldGenSettings.getDimensions();
            Set<Identifier> keySet = dimensions.getIds();
            HashMap<Identifier, ArrayList<ModdedBiomeSlice>> assignedSlices = new HashMap<>();
            for (Pair<ConditionedResourceSelector, ModdedBiomeSlice> unassignedSlice : unassignedSlices) {
                ModdedBiomeSlice slice = unassignedSlice.getSecond();
                if (slice.weight() <= 0) {
                    return;
                }
                unassignedSlice.getFirst().select(keySet::forEach).forEach(location -> assignedSlices.computeIfAbsent(location, id -> new ArrayList<>()).add(slice));
            }

            CommentedConfig moddedBiomeSliceSizes = BlueprintConfig.COMMON.moddedBiomeSliceSizes.get();
            int defaultSize = moddedBiomeSliceSizes.getIntOrElse("default", 9);
            if (defaultSize <= 0) {
                Blueprint.LOGGER.warn("Found a non-positive value for the default slice size! Slice size 9 will be used instead.");
                defaultSize = 9;
            }

            Registry<Biome> biomeRegistry = server.getRegistryManager().get(Registry.BIOME_KEY);
            long seed = worldGenSettings.getSeed();
            for (Map.Entry<RegistryKey<DimensionOptions>, DimensionOptions> entry : dimensions.getEntrySet()) {
                Identifier location = entry.getKey().getValue();
                ArrayList<ModdedBiomeSlice> slicesForKey = assignedSlices.get(location);
                if (slicesForKey != null && !slicesForKey.isEmpty()) {
                    ChunkGenerator chunkGenerator = entry.getValue().getChunkGenerator();
                    BiomeSource source = chunkGenerator.getBiomeSource();
                    //Checking specifically for an instance of MultiNoiseBiomeSource isn't reliable because mods may alter the biome source before we do
                    //If we do replace something we shouldn't then players can remove providers in a datapack
                    //TODO: Mostly experimental! Works with Terralith, Biomes O' Plenty, and more, but still needs more testing!
                    if (!(source instanceof FixedBiomeSource) && !(source instanceof CheckerboardBiomeSource)) {
                        int size = moddedBiomeSliceSizes.getIntOrElse(location.toString(), defaultSize);
                        if (size <= 0) {
                            size = defaultSize;
                        }
                        ModdedBiomeSource moddedBiomeSource = new ModdedBiomeSource(biomeRegistry, source, slicesForKey, size, seed, location.hashCode());
                        chunkGenerator.populationSource = moddedBiomeSource;
                        chunkGenerator.biomeSource = moddedBiomeSource;
                        if (chunkGenerator instanceof NoiseChunkGenerator)
                            ((ModdedSurfaceSystem)((NoiseChunkGenerator) chunkGenerator).surfaceBuilder).setModdedBiomeSource(moddedBiomeSource);
                    }
                }
            }
        });
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        List<Pair<ConditionedResourceSelector, ModdedBiomeSlice>> unassignedSlices = this.unassignedSlices;
        unassignedSlices.clear();
        RegistryOps<JsonElement> registryOps = this.registryOps;
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            Identifier name = entry.getKey();
            try {
                unassignedSlices.add(ModdedBiomeSlice.deserializeWithSelector(name, entry.getValue(), registryOps));
            } catch (JsonParseException exception) {
                Blueprint.LOGGER.error("Parsing error loading Modded Biome Slice: {}", name, exception);
            }
        }
        Blueprint.LOGGER.info("Modded Biome Slice Manager has loaded {} slices", unassignedSlices.size());
    }
}
