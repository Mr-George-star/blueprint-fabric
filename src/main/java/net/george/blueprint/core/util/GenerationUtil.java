package net.george.blueprint.core.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.TagKey;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

/**
 * This class holds a list of useful stuff for generation.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class GenerationUtil {
    public static final Predicate<BlockState> IS_AIR = AbstractBlock.AbstractBlockState::isAir;

    /**
     * Gets a predicate to check if a {@link BlockState} is an allowed fluid.
     *
     * @param minLevel      The minimum level the fluid be at.
     * @param allowedFluids A tag to use for the valid fluids.
     * @return A predicate to check if a {@link BlockState} is an allowed fluid.
     */
    public static Predicate<BlockState> isFluid(int minLevel, TagKey<Fluid> allowedFluids) {
        return (state) -> {
            FluidState fluid = state.getFluidState();
            return !fluid.isEmpty() && fluid.getHeight() >= minLevel && fluid.isIn(allowedFluids);
        };
    }

    /**
     * Fills a specified area using a given {@link WorldAccess} with a given {@link BlockState} if a given {@link Predicate} is met.
     *
     * @param world    A {@link WorldAccess} to use.
     * @param x1       Minimum x.
     * @param y1       Minimum y.
     * @param z1       Minimum z.
     * @param x2       Maximum x.
     * @param y2       Maximum y.
     * @param z2       Maximum z.
     * @param block    A {@link BlockState} to fill the area with.
     * @param canPlace If the {@link BlockState} can replace a found {@link BlockState}.
     */
    public static void fillAreaWithBlockCube(WorldAccess world, int x1, int y1, int z1, int x2, int y2, int z2, BlockState block, @Nullable Predicate<BlockState> canPlace) {
        BlockPos.Mutable positions = new BlockPos.Mutable();
        for (int xx = x1; xx <= x2; xx++) {
            for (int yy = y1; yy <= y2; yy++) {
                for (int zz = z1; zz <= z2; zz++) {
                    positions.set(xx, yy, zz);
                    if (canPlace == null || canPlace.test(world.getBlockState(positions))) {
                        world.setBlockState(positions, block, 2);
                    }
                }
            }
        }
    }

    /**
     * Fills a specified area using a given {@link WorldAccess} with random weighted states if a given {@link Predicate} is met.
     *
     * @param level    A {@link WorldAccess} to use.
     * @param random     A {@link Random} to use for randomizing the states.
     * @param x1       Minimum x.
     * @param y1       Minimum y.
     * @param z1       Minimum z.
     * @param x2       Maximum x.
     * @param y2       Maximum y.
     * @param z2       Maximum z.
     * @param canPlace If the {@link BlockState} can replace a found {@link BlockState}.
     * @param states   A {@link Pool} to use for selecting a random {@link BlockState}.
     */
    public static void fillAreaWithBlockCube(WorldAccess level, Random random, int x1, int y1, int z1, int x2, int y2, int z2, @Nullable Predicate<BlockState> canPlace, Pool<WeightedStateEntry> states) {
        BlockPos.Mutable positions = new BlockPos.Mutable();
        for (int xx = x1; xx <= x2; xx++) {
            for (int yy = y1; yy <= y2; yy++) {
                for (int zz = z1; zz <= z2; zz++) {
                    positions.set(xx, yy, zz);
                    if (canPlace == null || canPlace.test(level.getBlockState(positions))) {
                        level.setBlockState(positions, states.getOrEmpty(random).get().getState(), 2);
                    }
                }
            }
        }
    }

    /**
     * Outlines a specified area using a given {@link WorldAccess} with a given {@link BlockState} if a given {@link Predicate} is met.
     *
     * @param world    A {@link WorldAccess} to use.
     * @param x1       Minimum x.
     * @param y1       Minimum y.
     * @param z1       Minimum z.
     * @param x2       Maximum x.
     * @param y2       Maximum y.
     * @param z2       Maximum z.
     * @param block    A {@link BlockState} to fill the area with.
     * @param canPlace If the {@link BlockState} can replace a found {@link BlockState}.
     */
    public static void fillAreaWithBlockCubeEdged(WorldAccess world, int x1, int y1, int z1, int x2, int y2, int z2, BlockState block, @Nullable Predicate<BlockState> canPlace) {
        BlockPos.Mutable positions = new BlockPos.Mutable();
        for (int xx = x1; xx <= x2; xx++) {
            for (int yy = y1; yy <= y2; yy++) {
                for (int zz = z1; zz <= z2; zz++) {
                    positions.set(xx, yy, zz);
                    if ((canPlace == null || canPlace.test(world.getBlockState(positions))) && (xx == x2 || zz == z2)) {
                        world.setBlockState(positions, block, 2);
                    }
                }
            }
        }
    }

    /**
     * Outlines a specified area using a given {@link WorldAccess} with random weighted states if a given {@link Predicate} is met.
     *
     * @param world    A {@link WorldAccess} to use.
     * @param random     A {@link Random} to use for randomizing the states.
     * @param x1       Minimum x.
     * @param y1       Minimum y.
     * @param z1       Minimum z.
     * @param x2       Maximum x.
     * @param y2       Maximum y.
     * @param z2       Maximum z.
     * @param canPlace If the {@link BlockState} can replace a found {@link BlockState}.
     * @param states   A {@link Pool} to use for selecting a random {@link BlockState}.
     */
    public static void fillAreaWithBlockCubeEdged(WorldAccess world, Random random, int x1, int y1, int z1, int x2, int y2, int z2, @Nullable Predicate<BlockState> canPlace, Pool<WeightedStateEntry> states) {
        BlockPos.Mutable positions = new BlockPos.Mutable();
        for (int xx = x1; xx <= x2; xx++) {
            for (int yy = y1; yy <= y2; yy++) {
                for (int zz = z1; zz <= z2; zz++) {
                    positions.set(xx, yy, zz);
                    if ((canPlace == null || canPlace.test(world.getBlockState(positions))) && (xx == x2 || zz == z2)) {
                        world.setBlockState(positions, states.getOrEmpty(random).get().getState(), 2);
                    }
                }
            }
        }
    }

    /**
     * A {@link Weighted} implementation for storing weighted {@link BlockState}s.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public static class WeightedStateEntry implements Weighted {
        private final BlockState state;
        private final Weight weight;

        public WeightedStateEntry(BlockState state, int weight) {
            this.state = state;
            this.weight = Weight.of(weight);
        }

        /**
         * Gets this entry's {@link #state}.
         *
         * @return This entry's {@link #state}.
         */
        public BlockState getState() {
            return this.state;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }
}
