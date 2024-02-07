package net.george.blueprint.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Block} extension for vertical slab compatibility with the Quark mod.
 */
@SuppressWarnings("deprecation")
public class VerticalSlabBlock extends Block implements Waterloggable {
    public static final EnumProperty<VerticalSlabType> TYPE = EnumProperty.of("type", VerticalSlabType.class);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public VerticalSlabBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(TYPE, VerticalSlabType.NORTH).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE, WATERLOGGED);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return state.get(TYPE) != VerticalSlabType.DOUBLE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(TYPE).shape;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = context.getWorld().getBlockState(blockPos);
        if (blockState.getBlock() == this)
            return blockState.with(TYPE, VerticalSlabType.DOUBLE).with(WATERLOGGED, false);
        return this.getDefaultState().with(WATERLOGGED, context.getWorld().getFluidState(blockPos)
                .getFluid() == Fluids.WATER).with(TYPE, VerticalSlabType
                .fromDirection(this.getDirectionForPlacement(context)));
    }

    private Direction getDirectionForPlacement(ItemPlacementContext context) {
        Direction face = context.getSide();
        if (face.getAxis() != Direction.Axis.Y) return face;
        Vec3d difference = context.getHitPos().subtract(Vec3d.of(context.getBlockPos())).subtract(0.5, 0, 0.5);
        return Direction.fromRotation(-Math.toDegrees(Math.atan2(difference.getX(), difference.getZ()))).getOpposite();
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        VerticalSlabType slabType = state.get(TYPE);
        return slabType != VerticalSlabType.DOUBLE && context.getStack().getItem() == this.asItem() &&
                context.canReplaceExisting() && (context.getSide() == slabType.direction &&
                this.getDirectionForPlacement(context) == slabType.direction);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.get(TYPE) != VerticalSlabType.DOUBLE && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(TYPE) != VerticalSlabType.DOUBLE && Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return state;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return type == NavigationType.WATER && world.getFluidState(pos).isIn(FluidTags.WATER);
    }

    public static enum VerticalSlabType implements StringIdentifiable {
        NORTH(Direction.NORTH),
        SOUTH(Direction.SOUTH),
        WEST(Direction.WEST),
        EAST(Direction.EAST),
        DOUBLE(null);

        private final String name;
        @Nullable
        public final Direction direction;
        public final VoxelShape shape;

        VerticalSlabType(@Nullable Direction direction) {
            this.direction = direction;
            this.name = direction == null ? "double" : direction.asString();
            if (direction == null) {
                this.shape = VoxelShapes.fullCube();
            } else {
                boolean isNegativeAxis = direction.getDirection() == Direction.AxisDirection.NEGATIVE;
                double min = isNegativeAxis ? 8 : 0;
                double max = isNegativeAxis ? 16 : 8;
                this.shape = direction.getAxis() == Direction.Axis.X ? Block.createCuboidShape(min, 0, 0, max, 16, 16) : Block.createCuboidShape(0, 0, min, 16, 16, max);
            }
        }

        public static VerticalSlabType fromDirection(Direction direction) {
            for (VerticalSlabType type : VerticalSlabType.values()) {
                if (type.direction != null && direction == type.direction) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
