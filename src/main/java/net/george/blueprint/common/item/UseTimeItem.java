package net.george.blueprint.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An {@link Item} extension that has a defined use duration.
 */
public class UseTimeItem extends Item {
    private final int useTime;

    public UseTimeItem(int useTime, Settings settings) {
        super(settings);
        this.useTime = useTime;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.useTime;
    }
}
