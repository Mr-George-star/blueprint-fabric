package net.george.blueprint.common.block.wood;

import net.george.blueprint.common.block.BlueprintPressurePlateBlock;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link BlueprintPressurePlateBlock} extension that fills its item after the latest vanilla wooden pressure plate item.
 */
public class WoodPressurePlateBlock extends BlueprintPressurePlateBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_PRESSURE_PLATE);

    public WoodPressurePlateBlock(ActivationRule rule, Settings settings) {
        super(rule, settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
