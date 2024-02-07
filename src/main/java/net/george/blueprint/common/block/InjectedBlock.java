package net.george.blueprint.common.block;

import com.google.common.collect.Maps;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;

/**
 * A {@link Block} extension that fills its item after a defined {@link #followItem}.
 */
public class InjectedBlock extends Block {
    private static final Map<Item, TargetedItemCategoryFiller> FILLER_MAP = Maps.newHashMap();
    private final Item followItem;

    public InjectedBlock(Item followItem, Settings settings) {
        super(settings);
        this.followItem = followItem;
        FILLER_MAP.put(followItem, new TargetedItemCategoryFiller(() -> followItem));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER_MAP.get(this.followItem).fillItem(this.asItem(), group, stacks);
    }
}
