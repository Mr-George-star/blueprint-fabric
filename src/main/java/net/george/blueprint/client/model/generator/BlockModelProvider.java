package net.george.blueprint.client.model.generator;

import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * Stub class to extend for block model data providers, eliminates some
 * boilerplate constructor parameters.
 */
public abstract class BlockModelProvider extends ModelProvider<BlockModelBuilder> {
    public BlockModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, BLOCK_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @NotNull
    @Override
    public String getName() {
        return "Block Models: " + this.modid;
    }
}
