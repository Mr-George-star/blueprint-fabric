package net.george.blueprint.common.block.thatch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * A {@link StairsBlock} extension with certain methods overridden to accommodate models for thatch-type stairs.
 */
@SuppressWarnings("deprecation")
public class ThatchStairBlock extends StairsBlock {
    public ThatchStairBlock(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}
