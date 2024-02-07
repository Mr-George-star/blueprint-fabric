package net.george.blueprint.common.block.entity;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * A {@link ChestBlockEntity} extension used for Blueprint's chests.
 */
public class BlueprintChestBlockEntity extends ChestBlockEntity {
    protected BlueprintChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BlueprintChestBlockEntity(BlockPos pos, BlockState state) {
        super(BlueprintBlockEntityTypes.CHEST.get(), pos, state);
    }
}
