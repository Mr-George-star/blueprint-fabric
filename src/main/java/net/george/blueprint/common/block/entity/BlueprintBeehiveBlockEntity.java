package net.george.blueprint.common.block.entity;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * A {@link BeehiveBlockEntity} extension used for Blueprint's beehives.
 */
public class BlueprintBeehiveBlockEntity extends BeehiveBlockEntity {
    public BlueprintBeehiveBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Nonnull
    @Override
    public BlockEntityType<?> getType() {
        return BlueprintBlockEntityTypes.BEEHIVE.get();
    }
}
