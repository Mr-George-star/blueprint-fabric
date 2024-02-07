package net.george.blueprint.client.model.generator;

import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * Stub class to extend for item model data providers, eliminates some
 * boilerplate constructor parameters.
 */
public abstract class ItemModelProvider extends ModelProvider<ItemModelBuilder> {
    public ItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, ITEM_FOLDER, ItemModelBuilder::new, existingFileHelper);
    }

    @NotNull
    @Override
    public String getName() {
        return "Item Models: " + this.modid;
    }
}
