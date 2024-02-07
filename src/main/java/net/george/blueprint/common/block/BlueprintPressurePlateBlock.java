package net.george.blueprint.common.block;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

public class BlueprintPressurePlateBlock extends PressurePlateBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.STONE_PRESSURE_PLATE);

    public BlueprintPressurePlateBlock(ActivationRule rule, Settings settings) {
        super(rule, settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
