package net.george.blueprint.common.item;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.Item;

/**
 * An {@link Item} extension that stores a specified burn time for the item.
 */
public class FuelItem extends Item {
    public FuelItem(int burnTime, Settings settings) {
        super(settings);
        FuelRegistry.INSTANCE.add(this, burnTime);
    }
}
