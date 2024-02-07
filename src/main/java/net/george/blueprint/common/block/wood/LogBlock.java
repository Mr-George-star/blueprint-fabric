package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.DataUtil;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Supplier;

/**
 * A {@link PillarBlock} extension that fills its item after the latest vanilla log item.
 */
public class LogBlock extends PillarBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_STEM);

    public LogBlock(Supplier<Block> strippedBlock, Settings settings) {
        super(settings);
        DataUtil.registerStrippableBlock(this, strippedBlock.get());
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
