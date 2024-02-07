package net.george.blueprint.core;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.george.blueprint.core.data.server.modifiers.BlueprintModdedBiomeSliceProvider;
import net.george.blueprint.core.data.server.tags.BlueprintBlockTagsProvider;
import net.george.blueprint.core.data.server.tags.BlueprintItemTagsProvider;
import org.jetbrains.annotations.Nullable;

public class BlueprintDataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(BlueprintModdedBiomeSliceProvider::new);
        fabricDataGenerator.addProvider(BlueprintBlockTagsProvider::new);
        fabricDataGenerator.addProvider(BlueprintItemTagsProvider::new);
    }

    @Override
    @Nullable
    public String getEffectiveModId() {
        return Blueprint.MOD_ID;
    }
}
