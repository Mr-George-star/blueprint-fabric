package net.george.blueprint.core.util.item.filling;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * Interface implemented on classes for special filling of {@link Item}s in {@link ItemGroup}s.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
@FunctionalInterface
public interface IItemCategoryFiller {
    /**
     * Fills an {@link Item} for a {@link ItemGroup} given a {@link DefaultedList} of the {@link ItemStack}s for that {@link ItemGroup}.
     *
     * @param item  The {@link Item} to fill.
     * @param group   The {@link ItemGroup} to fill into.
     * @param items A {@link DefaultedList} of the {@link ItemStack}s for the {@link ItemGroup}.
     */
    void fillItem(Item item, ItemGroup group, DefaultedList<ItemStack> items);
}
