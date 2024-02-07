package net.george.blueprint.core.api;

import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.api.config.IConfigSpec;
import net.george.blueprint.core.api.config.ModConfig;

@SuppressWarnings("unused")
public class ModLoadingContext {
    public static ModConfig registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec) {
        return new ModConfig(type, spec, FabricLoader.getInstance().getModContainer(modId).orElseThrow(() ->
                new IllegalArgumentException(String.format("No mod with mod id %s", modId))));
    }

    public static ModConfig registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec, String fileName) {
        return new ModConfig(type, spec, FabricLoader.getInstance().getModContainer(modId).orElseThrow(() ->
                new IllegalArgumentException(String.format("No mod with mod id %s", modId))), fileName);
    }
}
