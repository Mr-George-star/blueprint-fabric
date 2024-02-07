package net.george.blueprint.common.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A {@link ItemDispenserBehavior} extension used to dispense the contents of a Fish {@link BucketItem}.
 */
public final class FishBucketDispenseItemBehavior extends ItemDispenserBehavior {
    @Override
    public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
        BucketItem bucketItem = (BucketItem) stack.getItem();
        BlockPos blockPos = source.getPos().offset(source.getBlockState().get(DispenserBlock.FACING));
        World world = source.getWorld();
        if (bucketItem.placeFluid(null, world, blockPos, null)) {
            bucketItem.onEmptied(null, world, stack, blockPos);
            return new ItemStack(Items.BUCKET);
        } else {
            return new ItemDispenserBehavior().dispense(source, stack);
        }
    }

}
