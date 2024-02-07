package net.george.blueprint.core.data.server.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.george.blueprint.core.other.tags.BlueprintBlockTags;
import net.minecraft.block.Blocks;

public class BlueprintBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public BlueprintBlockTagsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(BlueprintBlockTags.BOOKSHELVES).add(Blocks.BOOKSHELF);

        getOrCreateTagBuilder(BlueprintBlockTags.LEAF_PILES);
        getOrCreateTagBuilder(BlueprintBlockTags.HEDGES);
        getOrCreateTagBuilder(BlueprintBlockTags.LADDERS);
        getOrCreateTagBuilder(BlueprintBlockTags.VERTICAL_SLABS);
        getOrCreateTagBuilder(BlueprintBlockTags.WOODEN_VERTICAL_SLABS);
    }
}
