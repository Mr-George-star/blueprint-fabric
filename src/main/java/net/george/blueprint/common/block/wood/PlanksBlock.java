package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link Block} extension that fills its item after the latest vanilla planks item.
 */
public class PlanksBlock extends Block {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_PLANKS);

    public PlanksBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
