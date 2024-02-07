package net.george.blueprint.core.registry;

import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.registry.BiomeSubRegistryHelper;
import net.minecraft.world.biome.OverworldBiomeCreator;

public class BlueprintBiomes {
    private static final BiomeSubRegistryHelper HELPER = Blueprint.REGISTRY_HELPER.getBiomeSubHelper();

    public static final BiomeSubRegistryHelper.KeyedBiome ORIGINAL_SOURCE_MARKER = HELPER.createBiome("original_source_marker", OverworldBiomeCreator::createTheVoid);

    public static void register() {
        HELPER.register();
        Blueprint.LOGGER.debug("Registering Biomes for " + Blueprint.MOD_ID + "!");
    }
}
