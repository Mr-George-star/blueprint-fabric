package net.george.blueprint.core.util.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import javax.annotation.Nonnull;

/**
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class ItemStackUtil {
    private static final String[] M_NUMERALS = {"", "M", "MM", "MMM"};
    private static final String[] C_NUMERALS = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] X_NUMERALS = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] I_NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    /**
     * Searches for a specific item in a {@link DefaultedList} of {@link ItemStack} and returns its index.
     *
     * @param item  The item to search for.
     * @param items The list of {@link ItemStack}s.
     * @return The index of the specified item in the list, or -1 if it was not in the list.
     */
    public static int findIndexOfItem(Item item, DefaultedList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Used in {@link Item#appendStacks(ItemGroup, DefaultedList)} and {@link (ItemGroup, DefaultedList)} to fill an item after a specific item for a group.
     *
     * @param item       The item to fill.
     * @param targetItem The item to fill after.
     * @param group        The group to fill it in.
     * @param items      The {@link DefaultedList} of item stacks to search for the target item and inject the item in.
     */
    public static void fillAfterItemForCategory(Item item, Item targetItem, ItemGroup group, DefaultedList<ItemStack> items) {
        if (isAllowedInTab(item, group)) {
            int targetIndex = findIndexOfItem(targetItem, items);
            if (targetIndex != -1) {
                items.add(targetIndex + 1, new ItemStack(item));
            } else {
                items.add(new ItemStack(item));
            }
        }
    }

    /**
     * Converts an Integer to a String of Roman Numerals; useful for levels.
     *
     * @param number The integer to convert.
     * @return The integer converted to roman numerals.
     */
    public static String intToRomanNumerals(int number) {
        String thousands = M_NUMERALS[number / 1000];
        String hundreds = C_NUMERALS[(number % 1000) / 100];
        String tens = X_NUMERALS[(number % 100) / 10];
        String ones = I_NUMERALS[number % 10];
        return thousands + hundreds + tens + ones;
    }

    /**
     * Checks if an {@link Item} is in an {@link ItemGroup}.
     *
     * @param item The {@link Item} to check.
     * @param group  The {@link ItemGroup} to check.
     * @return Whether the item is in the {@link ItemGroup}.
     */
    public static boolean isAllowedInTab(Item item, @Nonnull ItemGroup group) {
        return item.isIn(group);
    }
}
