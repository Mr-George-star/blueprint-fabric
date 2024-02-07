package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link FenceGateBlock} extension that fills its item after the latest vanilla wooden fence gate item.
 */
public class WoodFenceGateBlock extends FenceGateBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_FENCE_GATE);

    public WoodFenceGateBlock(Settings properties) {
        super(properties);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
