package net.george.blueprint.core.data.server.modifiers;

import net.george.blueprint.common.world.modification.ModdedBiomeSliceProvider;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.BiomeUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.dimension.DimensionOptions;

/**
 * A {@link ModdedBiomeSliceProvider} subclass that generates Blueprint's built-in modded biome slices.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class BlueprintModdedBiomeSliceProvider extends ModdedBiomeSliceProvider {
    public BlueprintModdedBiomeSliceProvider(DataGenerator dataGenerator) {
        super(dataGenerator, Blueprint.MOD_ID);
    }

    @Override
    protected void registerSlices() {
        this.registerSlice("originals", 10, new BiomeUtil.OriginalModdedBiomeProvider(),
                DimensionOptions.OVERWORLD.getValue(), DimensionOptions.NETHER.getValue(), DimensionOptions.END.getValue());
    }
}
