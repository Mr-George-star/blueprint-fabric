package net.george.blueprint.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A utility class containing some useful methods for trees.
 *
 * @author bageldotjpg
 */
@SuppressWarnings({"unused"})
@ApiStatus.NonExtendable
public class TreeUtil {
    /**
     * Places a log at a {@link BlockPos} using a given {@link StructureWorldAccess}, {@link Random}, and {@link TreeFeatureConfig}.
     *
     * @param world  A {@link StructureWorldAccess} to use for placing the log.
     * @param pos    A {@link BlockPos} for where to place the log.
     * @param random   A {@link Random} for randomly selecting the log state.
     * @param config A {@link TreeFeatureConfig} to select the log state.
     */
    public static void placeLogAt(StructureWorldAccess world, BlockPos pos, Random random, TreeFeatureConfig config) {
        setForcedState(world, pos, config.trunkProvider.getBlockState(random, pos));
    }

    /**
     * Places a directional log at a {@link BlockPos} using a given {@link StructureWorldAccess}, {@link Random}, and {@link TreeFeatureConfig}.
     *
     * @param world     A {@link StructureWorldAccess} to use for placing the log.
     * @param pos       A {@link BlockPos} for where to place the log.
     * @param direction The {@link Direction} of the log.
     * @param random      A {@link Random} for randomly selecting the log state.
     * @param config    A {@link TreeFeatureConfig} to select the log state.
     */
    public static void placeDirectionalLogAt(StructureWorldAccess world, BlockPos pos, Direction direction, Random random, TreeFeatureConfig config) {
        setForcedState(world, pos, config.trunkProvider.getBlockState(random, pos).with(Properties.AXIS, direction.getAxis()));
    }

    /**
     * Checks if a {@link BlockPos} has a state at its position in a given tag.
     *
     * @param world A {@link WorldView} for getting the state at the given pos.
     * @param pos   A {@link BlockPos} to look up the block.
     * @param tag   A tag to check.
     * @return If a {@link BlockPos} has a state at its position in a given tag.
     */
    public static boolean isInTag(WorldView world, BlockPos pos, TagKey<Block> tag) {
        return world.getBlockState(pos).isIn(tag);
    }

    /**
     * Places a foliage block at a {@link BlockPos} using a given {@link StructureWorldAccess}, {@link Random}, and {@link TreeFeatureConfig}.
     *
     * @param world  A {@link StructureWorldAccess} for placing the foliage.
     * @param pos    A {@link BlockPos} for where to place the foliage.
     * @param random   A {@link Random} for randomly selecting the foliage state.
     * @param config A {@link TreeFeatureConfig} to select the foliage state.
     */
    public static void placeLeafAt(StructureWorldAccess world, BlockPos pos, Random random, TreeFeatureConfig config) {
        if (TreeFeature.canReplace(world, pos)) {
            setForcedState(world, pos, config.foliageProvider.getBlockState(random, pos));
        }
    }

    /**
     * Forcefully sets a {@link BlockState} at a given {@link BlockPos}.
     * <p>Uses flag 19.</p>
     *
     * @param world A {@link StructureWorldAccess} to use for placing the {@link BlockState}.
     * @param pos   A {@link BlockPos} for where to place the {@link BlockState}.
     * @param state A {@link BlockState} to place.
     */
    public static void setForcedState(StructureWorldAccess world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, 19);
    }

    /**
     * Checks if there is a {@link BlockState} tagged as {@link BlockTags#LOGS} at a given {@link BlockPos}.
     *
     * @param world A {@link WorldView} for getting the {@link BlockState}.
     * @param pos   A {@link BlockPos} for where to check.
     * @return If there is a {@link BlockState} tagged as {@link BlockTags#LOGS} at a given {@link BlockPos}.
     */
    public static boolean isLog(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).isIn(BlockTags.LOGS);
    }

    /**
     * Checks if there is a {@link BlockState} tagged as {@link BlockTags#LEAVES} at a given {@link BlockPos}.
     *
     * @param world A {@link WorldView} for getting the {@link BlockState}.
     * @param pos   A {@link BlockPos} for where to check.
     * @return If there is a {@link BlockState} tagged as {@link BlockTags#LEAVES} at a given {@link BlockPos}.
     */
    public static boolean isLeaves(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).isIn(BlockTags.LEAVES);
    }

    /**
     * Checks if there is a {@link BlockState} tagged as {@link BlockTags#LEAVES} or is air at a given {@link BlockPos}.
     *
     * @param world A {@link WorldView} for getting the {@link BlockState}.
     * @param pos   A {@link BlockPos} for where to check.
     * @return If there is a {@link BlockState} tagged as {@link BlockTags#LEAVES} or is air at a given {@link BlockPos}.
     */
    public static boolean isAirOrLeaves(WorldView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.isIn(BlockTags.LEAVES);
    }

    /**
     * Sets dirt at a given {@link BlockPos} if there is grass or farmland at the given {@link BlockPos}.
     *
     * @param world A {@link WorldAccess} to use.
     * @param pos   A {@link BlockPos} for where the dirt get placed.
     */
    public static void setDirtAt(WorldAccess world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 19);
        }
    }

    /**
     * Checks if the {@link BlockState} at a given {@link BlockPos} can sustain a given {@link SaplingBlock}.
     *
     * @param world   A {@link WorldAccess} to use.
     * @param pos     A {@link BlockPos} for where to check.
     * @param sapling A {@link SaplingBlock} to check for.
     * @return If the {@link BlockState} at a given {@link BlockPos} can sustain a given {@link SaplingBlock}.
     */
    public static boolean isValidGround(WorldAccess world, BlockPos pos, SaplingBlock sapling) {
        return sapling.canPlaceAt(world.getBlockState(pos), world, pos);
    }

    /**
     * Updates the distances of nearby leaves around a set of logPositions
     *
     * @param world        A {@link WorldAccess} to use.
     * @param logPositions A {@link Set} of {@link BlockPos} where logs are at.
     */
    public static void updateLeaves(WorldAccess world, Set<BlockPos> logPositions) {
        List<Set<BlockPos>> list = Lists.newArrayList();

        for (int j = 0; j < 6; ++j) {
            list.add(Sets.newHashSet());
        }
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (BlockPos pos : Lists.newArrayList(logPositions)) {
            for (Direction direction : Direction.values()) {
                mutablePos.set(pos, direction);
                if (!logPositions.contains(mutablePos)) {
                    BlockState state = world.getBlockState(mutablePos);
                    if (state.contains(Properties.DISTANCE_1_7)) {
                        list.get(0).add(mutablePos.toImmutable());
                        TreeUtil.setForcedState((StructureWorldAccess) world, mutablePos, state.with(Properties.DISTANCE_1_7, 1));
                    }
                }
            }
        }

        for (int l = 1; l < 6; ++l) {
            Set<BlockPos> set = list.get(l - 1);
            Set<BlockPos> set1 = list.get(l);

            for (BlockPos pos : set) {
                for (Direction direction1 : Direction.values()) {
                    mutablePos.set(pos, direction1);
                    if (!set.contains(mutablePos) && !set1.contains(mutablePos)) {
                        BlockState state = world.getBlockState(mutablePos);
                        if (state.contains(Properties.DISTANCE_1_7)) {
                            int k = state.get(Properties.DISTANCE_1_7);
                            if (k > l + 1) {
                                TreeUtil.setForcedState((StructureWorldAccess) world, mutablePos, state.with(Properties.DISTANCE_1_7, l + 1));
                                set1.add(mutablePos.toImmutable());
                            }
                        }
                    }
                }
            }
        }
    }
}
