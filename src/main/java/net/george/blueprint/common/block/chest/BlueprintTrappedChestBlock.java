package net.george.blueprint.common.block.chest;

import net.george.blueprint.common.block.entity.BlueprintTrappedChestBlockEntity;
import net.george.blueprint.core.api.IChestBlock;
import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

/**
 * A {@link ChestBlock} extension used for Blueprint's trapped chests.
 */
@SuppressWarnings("deprecation")
public class BlueprintTrappedChestBlock extends ChestBlock implements IChestBlock {
    public final String type;

    public BlueprintTrappedChestBlock(String type, Settings settings) {
        super(settings, BlueprintBlockEntityTypes.TRAPPED_CHEST::get);
        this.type = type;
    }

    @Override
    public String getChestType() {
        return this.type;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintTrappedChestBlockEntity(pos, state);
    }

    @Override
    protected Stat<Identifier> getOpenStat() {
        return Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return MathHelper.clamp(ChestBlockEntity.getPlayersLookingInChestCount(world, pos), 0, 15);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : 0;
    }
}
