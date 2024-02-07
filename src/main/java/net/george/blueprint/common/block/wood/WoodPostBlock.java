package net.george.blueprint.common.block.wood;

import net.george.blueprint.core.util.DataUtil;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A {@link Block} extension for wooden posts.
 */
@SuppressWarnings("deprecation")
public class WoodPostBlock extends Block implements Waterloggable {
    private static final VoxelShape SHAPE_X = Block.createCuboidShape(0.0F, 6.0F, 6.0F, 16.0F, 10.0F, 10.0F);
    private static final VoxelShape SHAPE_Y = Block.createCuboidShape(6.0F, 0.0F, 6.0F, 10.0F, 16.0F, 10.0F);
    private static final VoxelShape SHAPE_Z = Block.createCuboidShape(6.0F, 6.0F, 0.0F, 10.0F, 10.0F, 16.0F);
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    private static final BooleanProperty[] CHAINED = new BooleanProperty[] {
            BooleanProperty.of("chain_down"),
            BooleanProperty.of("chain_up"),
            BooleanProperty.of("chain_north"),
            BooleanProperty.of("chain_south"),
            BooleanProperty.of("chain_west"),
            BooleanProperty.of("chain_east")
    };

    public WoodPostBlock(Settings settings) {
        this(null, settings);
    }

    public WoodPostBlock(Supplier<Block> block, Settings settings) {
        super(settings);
        BlockState defaultState = this.getStateManager().getDefaultState().with(WATERLOGGED, false).with(AXIS, Direction.Axis.Y);
        for (BooleanProperty property : CHAINED)
            defaultState = defaultState.with(property, false);
        this.setDefaultState(defaultState);
        DataUtil.registerStrippableBlock(this, block.get());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            default -> SHAPE_Z;
        };
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return !state.get(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getRelevantState(context.getWorld(), context.getBlockPos(), context.getSide().getAxis());
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        BlockState newState = this.getRelevantState(world, pos, state.get(AXIS));
        if (!newState.equals(state))
            world.setBlockState(pos, newState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, AXIS);
        for (BooleanProperty property : CHAINED)
            builder.add(property);
    }

    private BlockState getRelevantState(World world, BlockPos pos, Direction.Axis axis) {
        BlockState state = this.getDefaultState().with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER).with(AXIS, axis);

        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis)
                continue;

            BlockState sideState = world.getBlockState(pos.offset(direction));
            if ((sideState.getBlock() instanceof ChainBlock && sideState.get(Properties.AXIS) == direction.getAxis())
                    || (direction == Direction.DOWN && sideState.getBlock() instanceof LanternBlock && sideState.get(LanternBlock.HANGING)))
                state = state.with(CHAINED[direction.ordinal()], true);
        }

        return state;
    }
}
