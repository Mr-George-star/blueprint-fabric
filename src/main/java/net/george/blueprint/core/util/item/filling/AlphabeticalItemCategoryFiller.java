package net.george.blueprint.core.util.item.filling;

import net.george.blueprint.core.util.ItemUtil;
import net.george.blueprint.core.util.item.ItemStackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Predicate;

/**
 * Implementation class of {@link IItemCategoryFiller} for filling {@link Item}s alphabetically.
 * <p>{@link #shouldInclude} is used to test what items should be considered when inserting an item at its alphabetical position.</p>
 *
 * @author SmellyModder (Luke Tonon)
 * @see IItemCategoryFiller
 */
@SuppressWarnings("unused")
public final class AlphabeticalItemCategoryFiller implements IItemCategoryFiller {
    private final Predicate<Item> shouldInclude;

    public AlphabeticalItemCategoryFiller(Predicate<Item> shouldInclude) {
        this.shouldInclude = shouldInclude;
    }

    /**
     * Creates an {@link AlphabeticalItemCategoryFiller} that fills items alphabetically for items that are an instance of a class. (e.g. Having a modded spawn egg filled alphabetically into the vanilla's spawn eggs)
     *
     * @param clazz The class to test for.
     * @param <I>   The type of the class, must extend {@link Item}.
     * @return An {@link AlphabeticalItemCategoryFiller} that fills items alphabetically for items that are an instance of a class. (e.g. Having a modded spawn egg filled alphabetically into the vanilla's spawn eggs)
     */
    public static <I extends Item> AlphabeticalItemCategoryFiller forClass(Class<I> clazz) {
        return new AlphabeticalItemCategoryFiller(clazz::isInstance);
    }

    @Override
    public void fillItem(Item item, ItemGroup group, DefaultedList<ItemStack> items) {
        if (ItemStackUtil.isAllowedInTab(item, group)) {
            Identifier id = ItemUtil.getItemId(item);
            if (id != null) {
                String itemName = id.getPath();
                int insert = -1;
                Predicate<Item> shouldInclude = this.shouldInclude;
                for (int i = 0; i < items.size(); i++) {
                    Item next = items.get(i).getItem();
                    if (shouldInclude.test(next)) {
                        Identifier nextName = ItemUtil.getItemId(next);
                        if (nextName == null || itemName.compareTo(nextName.getPath()) > 0) {
                            insert = i + 1;
                        } else if (insert == -1) {
                            insert += i + 1;
                        } else {
                            break;
                        }
                    }
                }
                if (insert == -1) {
                    items.add(new ItemStack(item));
                } else {
                    items.add(insert, new ItemStack(item));
                }
            } else {
                items.add(new ItemStack(item));
            }
        }
    }
}
