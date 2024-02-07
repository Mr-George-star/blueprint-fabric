package net.george.blueprint.common.block.thatch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.common.block.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * A {@link VerticalSlabBlock} extension with certain methods overridden to accommodate models for thatch-type vertical slabs.
 */
@SuppressWarnings("deprecation")
public class ThatchVerticalSlabBlock extends VerticalSlabBlock {
    public ThatchVerticalSlabBlock(Settings settings) {
        super(settings);
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
