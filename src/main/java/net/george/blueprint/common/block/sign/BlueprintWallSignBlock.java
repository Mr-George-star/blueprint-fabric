package net.george.blueprint.common.block.sign;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.SignType;
import net.minecraft.util.math.BlockPos;

/**
 * A {@link WallSignBlock} extension used for Blueprint's wall signs.
 */
public class BlueprintWallSignBlock extends WallSignBlock implements IBlueprintSign {
    public BlueprintWallSignBlock(Settings settings, SignType type) {
        super(settings, type);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlueprintBlockEntityTypes.SIGN.get().instantiate(pos, state);
    }
}
