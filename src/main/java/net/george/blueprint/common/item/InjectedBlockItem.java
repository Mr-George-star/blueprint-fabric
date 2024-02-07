package net.george.blueprint.common.item;

import com.google.common.collect.Maps;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;

/**
 * A {@link BlockItem} extension that fills itself after a defined {@link #followItem}.
 */
public class InjectedBlockItem extends BlockItem {
    private static final Map<Item, TargetedItemCategoryFiller> FILLER_MAP = Maps.newHashMap();
    private final Item followItem;

    public InjectedBlockItem(Item followItem, Block block, Settings builder) {
        super(block, builder);
        this.followItem = followItem;
        FILLER_MAP.put(followItem, new TargetedItemCategoryFiller(() -> followItem));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER_MAP.get(this.followItem).fillItem(this.asItem(), group, stacks);
    }
}
