package net.george.blueprint.common.block;

import com.google.common.base.Preconditions;
import net.george.blueprint.core.other.tags.BlueprintBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Block} extension for hedge compatibility with the Quark mod.
 */
@SuppressWarnings("deprecation")
public class HedgeBlock extends FenceBlock {
    private static final BooleanProperty EXTEND = BooleanProperty.of("extend");

    public HedgeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(EXTEND, false));
    }

    @Override
    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {
        return state.isIn(BlueprintBlockTags.HEDGES);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !state.get(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Preconditions.checkNotNull(super.getPlacementState(context));
        return super.getPlacementState(context).with(EXTEND, context.getWorld().getBlockState(context.getBlockPos().down()).isIn(BlueprintBlockTags.HEDGES));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return direction == Direction.DOWN ? state.with(EXTEND, neighborState.isIn(BlueprintBlockTags.HEDGES)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(EXTEND);
    }
}
