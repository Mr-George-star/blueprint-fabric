package net.george.blueprint.common.block.chest;

import net.george.blueprint.common.block.entity.BlueprintChestBlockEntity;
import net.george.blueprint.core.api.IChestBlock;
import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * A {@link ChestBlock} extension used for Blueprint's chests.
 */
public class BlueprintChestBlock extends ChestBlock implements IChestBlock {
    public final String type;

    public BlueprintChestBlock(String type, Settings settings) {
        super(settings, BlueprintBlockEntityTypes.CHEST::get);
        this.type = type;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintChestBlockEntity(pos, state);
    }

    @Override
    public String getChestType() {
        return type;
    }
}
