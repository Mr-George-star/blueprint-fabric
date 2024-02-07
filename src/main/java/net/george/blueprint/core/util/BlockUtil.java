package net.george.blueprint.core.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A class containing some utility methods related to blocks.
 *
 * @author SmellyModder (Luke Tonon)
 * @author abigailfails
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class BlockUtil {
    /**
     * Checks if there is a block in water at a given {@link BlockPos} in a {@link World}.
     *
     * @param world A {@link World} to use for checking the state at the given pos.
     * @param pos   A {@link BlockPos} to check at.
     * @return If there is a block in water at a given {@link BlockPos} in a {@link World}.
     */
    public static boolean isBlockInWater(World world, BlockPos pos) {
        if (world.getBlockState(pos).getFluidState().isIn(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (world.getFluidState(pos.offset(direction)).isIn(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a player can place a {@link BlockState} at a given {@link BlockPos} in a given {@link World}.
     *
     * @param world  A {@link World} to use for checking the state at the given pos.
     * @param player A {@link PlayerEntity} to use for collision context, or null for no context.
     * @param pos    A {@link BlockPos} to check at.
     * @param state  A {@link BlockState} to check.
     * @return If a player can place a {@link BlockState} at a given {@link BlockPos} in a given {@link World}.
     */
    public static boolean canPlace(World world, @Nullable PlayerEntity player, BlockPos pos, BlockState state) {
        ShapeContext selectionContext = player == null ? ShapeContext.absent() : ShapeContext.of(player);
        VoxelShape voxelShape = state.getCollisionShape(world, pos, selectionContext);
        VoxelShape offsetShape = world.getBlockState(pos).getCollisionShape(world, pos);
        return (offsetShape.isEmpty() || world.getBlockState(pos).getMaterial().isReplaceable()) && state.canPlaceAt(world, pos) && world.doesNotIntersectEntities(null, voxelShape.offset(pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * Gets the place sound of a given {@link BlockState}.
     *
     * @param state  A {@link BlockState} to get the place sound of.
     * @return The place sound of a given {@link BlockState}.
     */
    public static SoundEvent getPlaceSound(BlockState state) {
        return state.getBlock().getSoundGroup(state).getPlaceSound();
    }

    /**
     * Checks if a given {@link BlockPos} is not touching another specified block.
     *
     * @param level                 A {@link World} to use.
     * @param pos                   A {@link BlockPos} to check.
     * @param blockToCheck          A {@link Block} to check.
     * @param blacklistedDirections An array of directions to not check if the position is touching.
     * @return If a given {@link BlockPos} is not touching another specified block.
     */
    public static boolean isPosNotTouchingBlock(WorldAccess level, BlockPos pos, Block blockToCheck, Direction... blacklistedDirections) {
        for (Direction directions : Direction.values()) {
            List<Direction> blacklistedDirectionsList = Arrays.asList(blacklistedDirections);
            if (!blacklistedDirectionsList.contains(directions)) {
                if (level.getBlockState(pos.offset(directions)).getBlock() == blockToCheck) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies all the similar properties of one {@link BlockState} to another.
     *
     * @param initial The {@link BlockState} to copy from.
     * @param after   The {@link BlockState} to copy to.
     * @return A {@link BlockState} containing all the similar properties of one {@link BlockState} copied from another.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BlockState transferAllBlockStates(BlockState initial, BlockState after) {
        BlockState block = after;
        for (Property property : initial.getBlock().getStateManager().getProperties()) {
            if (after.contains(property) && initial.get(property) != null) {
                block = block.with(property, initial.get(property));
            }
        }
        return block;
    }

    /**
     * Rotates a given {@link Box} using a given {@link BBRotation}.
     *
     * @param box       An {@link Box} to rotate.
     * @param rotation A {@link BBRotation} to use.
     * @return A rotated {@link Box}.
     */
    public static Box rotateHorizontalBB(Box box, BBRotation rotation) {
        return rotation.rotateBB(box);
    }

    /**
     * <p>Returns the {@link BlockPos} offset by 1 in the direction of {@code source}'s {@link BlockState}'s
     * {@link FacingBlock#FACING} property.</p>
     * This requires the {@link BlockState} stored in {@code source} to have a {@link FacingBlock#FACING} property.
     *
     * @param source The {@link BlockPointer} to get the position from.
     * @return The position in front of the dispenser's output face.
     * @author abigailfails
     */
    public static BlockPos offsetPos(BlockPointer source) {
        return source.getPos().offset(source.getBlockState().get(FacingBlock.FACING));
    }

    /**
     * Gets the {@link BlockState} at the position returned by {@link #offsetPos(BlockPointer)}.
     *
     * @param source The {@link BlockPointer} to get the position from.
     * @return The {@link BlockState} at the offset position.
     * @see #offsetPos(BlockPointer)
     */
    public static BlockState getStateAtOffsetPos(BlockPointer source) {
        return source.getWorld().getBlockState(offsetPos(source));
    }

    /**
     * Gets a {@link List} of type {@link T} at the position returned by {@link #offsetPos(BlockPointer)}.
     *
     * @param source     The {@link BlockPos} to get the position from.
     * @param entityType The class extending {@link T} to search for. Set to {@code Entity.class} to get all entities, regardless of type.
     * @return A {@link List} of entities at the offset position.
     * @see #offsetPos(BlockPointer)
     */
    public static <T extends Entity> List<T> getEntitiesAtOffsetPos(BlockPointer source, Class<T> entityType) {
        return source.getWorld().getNonSpectatingEntities(entityType, new Box(offsetPos(source)));
    }

    /**
     * Gets a {@link List} of type {@link T} that match a {@link Predicate} at the position returned by {@link #offsetPos(BlockPointer)}.
     *
     * @param source     The {@link BlockPointer} to get the position from.
     * @param entityType The class extending {@link T} to search for. Set to {@code Entity.class} to get all entities, regardless of type.
     * @param predicate  The predicate that takes a superclass of {@link T} as an argument to check against.
     * @return A {@link List} of entities at the offset position.
     * @see #offsetPos(BlockPointer)
     */
    public static <T extends Entity> List<T> getEntitiesAtOffsetPos(BlockPointer source, Class<T> entityType, Predicate<? super T> predicate) {
        return source.getWorld().getEntitiesByClass(entityType, new Box(offsetPos(source)), predicate);
    }

    /**
     * An enum containing types for rotating an {@link Box}.
     */
    public enum BBRotation {
        REVERSE_X((bb) -> {
            final float minX = 1.0F - (float) bb.maxX;
            return new Box(minX, bb.minY, bb.minZ, bb.maxX >= 1.0F ? bb.maxX - bb.minX : bb.maxX + minX, bb.maxY, bb.maxZ);
        }),
        REVERSE_Z((bb) -> {
            final float minZ = 1.0F - (float) bb.maxZ;
            return new Box(bb.minX, bb.minY, minZ, bb.maxX, bb.maxY, bb.maxZ >= 1.0F ? bb.maxZ - bb.minZ : bb.maxZ + minZ);
        }),
        RIGHT((bb) -> new Box( bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX)),
        LEFT((bb) -> REVERSE_X.rotateBB(RIGHT.rotateBB(bb)));

        private final UnaryOperator<Box> modifier;

        BBRotation(UnaryOperator<Box> modifier) {
            this.modifier = modifier;
        }

        /**
         * Gets the {@link BBRotation} to use for a given current {@link Direction} and starting {@link Direction}.
         *
         * @param currentDirection  A current {@link Direction}.
         * @param startingDirection A starting {@link Direction}.
         * @return The {@link BBRotation} to use for a given current {@link Direction} and starting {@link Direction}.
         */
        public static BBRotation getRotationForDirection(Direction currentDirection, Direction startingDirection) {
            int currentIndex = currentDirection.getId() - 2;
            int startingIndex = startingDirection.getId() - 2;
            int index = (currentIndex - startingIndex) % 4;

            return switch (index) {
                default -> BBRotation.REVERSE_X;
                case 1 -> BBRotation.REVERSE_Z;
                case 2 -> BBRotation.RIGHT;
                case 3 -> BBRotation.LEFT;
            };
        }

        /**
         * Rotates a given {@link Box}.
         *
         * @param box An {@link Box} to rotate.
         * @return A rotated {@link Box}.
         */
        public Box rotateBB(Box box) {
            return this.modifier.apply(box);
        }
    }
}
