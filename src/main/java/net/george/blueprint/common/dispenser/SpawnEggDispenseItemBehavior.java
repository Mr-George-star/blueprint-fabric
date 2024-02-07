package net.george.blueprint.common.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;

/**
 * A {@link ItemDispenserBehavior} extension used to dispense {@link SpawnEggItem} instances.
 */
public final class SpawnEggDispenseItemBehavior extends ItemDispenserBehavior {
    @Override
    public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
        Direction direction = source.getBlockState().get(DispenserBlock.FACING);
        EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getEntityType(stack.getNbt());
        entityType.spawnFromItemStack(source.getWorld(), stack, null, source.getPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
        stack.decrement(1);
        return stack;
    }
}