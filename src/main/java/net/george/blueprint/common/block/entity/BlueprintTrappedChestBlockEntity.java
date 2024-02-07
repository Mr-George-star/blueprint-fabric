package net.george.blueprint.common.block.entity;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A {@link BlueprintChestBlockEntity} extension used for Blueprint's trapped chests.
 */
public class BlueprintTrappedChestBlockEntity extends BlueprintChestBlockEntity {
    public BlueprintTrappedChestBlockEntity(BlockPos pos, BlockState state) {
        super(BlueprintBlockEntityTypes.TRAPPED_CHEST.get(), pos, state);
    }

    @Override
    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        super.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
        if (oldViewerCount != newViewerCount) {
            Block block = state.getBlock();
            world.updateNeighborsAlways(pos, block);
            world.updateNeighborsAlways(pos.down(), block);
        }
    }
}
