package net.george.blueprint.common.block.entity;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * A {@link SignBlockEntity} extension used for Blueprint's signs.
 */
public class BlueprintSignBlockEntity extends SignBlockEntity {
    public BlueprintSignBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return BlueprintBlockEntityTypes.SIGN.get();
    }
}
