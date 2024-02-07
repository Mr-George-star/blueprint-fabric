package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.DoorBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

/**
 * A {@link DoorBlock} extension that fills its item after the latest vanilla wooden door item.
 */
public class WoodDoorBlock extends DoorBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_DOOR);

    public WoodDoorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
