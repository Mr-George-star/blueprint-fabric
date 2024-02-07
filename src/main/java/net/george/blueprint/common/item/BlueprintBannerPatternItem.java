package net.george.blueprint.common.item;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link BannerPatternItem} extension that fills itself after the latest vanilla banner pattern item.
 */
public class BlueprintBannerPatternItem extends BannerPatternItem {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.PIGLIN_BANNER_PATTERN);

    public BlueprintBannerPatternItem(BannerPattern pattern, Settings builder) {
        super(pattern, builder);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this, group, stacks);
    }
}
