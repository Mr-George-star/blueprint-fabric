package net.george.blueprint.core.data.server.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.george.blueprint.core.other.tags.BlueprintBlockTags;
import net.george.blueprint.core.other.tags.BlueprintItemTags;

public class BlueprintItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public BlueprintItemTagsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(BlueprintItemTags.BOATABLE_CHESTS);
        copy(BlueprintBlockTags.LADDERS, BlueprintItemTags.LADDERS);
        copy(BlueprintBlockTags.VERTICAL_SLABS, BlueprintItemTags.VERTICAL_SLABS);
        copy(BlueprintBlockTags.WOODEN_VERTICAL_SLABS, BlueprintItemTags.WOODEN_VERTICAL_SLABS);
    }
}
