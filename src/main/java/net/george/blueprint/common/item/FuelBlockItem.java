package net.george.blueprint.common.item;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

/**
 * A {@link BlockItem} extension that stores a specified burn time for the item.
 */
public class FuelBlockItem extends BlockItem {
    public FuelBlockItem(Block block, int burnTime, Settings settings) {
        super(block, settings);
        FuelRegistry.INSTANCE.add(this, burnTime);
    }
}
