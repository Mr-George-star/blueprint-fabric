package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.WoodenButtonBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link WoodenButtonBlock} extension that fills its item after the latest vanilla wood button item.
 */
public class BlueprintWoodButtonBlock extends WoodenButtonBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_BUTTON);

    public BlueprintWoodButtonBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
