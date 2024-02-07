package net.george.blueprint.core.util;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Class that makes checking conditions for placing generation parts easier.
 *
 * @author SmellyModder (Luke Tonon)
 */
public class GenerationPiece {
    private final List<BlockPart> blockPieces = Lists.newArrayList();
    private final BiPredicate<WorldAccess, BlockPart> blockPlaceCondition;

    public GenerationPiece(BiPredicate<WorldAccess, BlockPart> blockPlaceCondition) {
        this.blockPlaceCondition = blockPlaceCondition;
    }

    /**
     * Adds a block to this piece.
     *
     * @param state The state of the block.
     * @param pos   The position of the block.
     */
    public void addBlockPiece(BlockState state, BlockPos pos) {
        this.blockPieces.add(new BlockPart(state, pos));
    }

    /**
     * Checks if all the blocks loaded in this piece can be placed.
     *
     * @param level The level to place the piece in.
     * @return If all the blocks loaded in this piece can be placed.
     */
    public boolean canPlace(WorldAccess level) {
        for (BlockPart blocks : this.blockPieces) {
            if (!this.blockPlaceCondition.test(level, blocks)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Places the piece.
     *
     * @param level The level to place the piece in.
     */
    public void place(WorldAccess level) {
        for (BlockPart blocks : this.blockPieces) {
            level.setBlockState(blocks.pos, blocks.state, 2);
        }
    }

    /**
     * Sees if the piece can be placed and then if it can, places it.
     *
     * @param level The level to place the piece in.
     */
    public void tryToPlace(WorldAccess level) {
        if (this.canPlace(level)) {
            this.place(level);
        }
    }

    public static class BlockPart {
        public final BlockState state;
        public final BlockPos pos;

        public BlockPart(BlockState state, BlockPos pos) {
            this.state = state;
            this.pos = pos;
        }
    }
}
