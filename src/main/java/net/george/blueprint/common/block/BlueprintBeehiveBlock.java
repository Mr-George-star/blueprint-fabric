package net.george.blueprint.common.block;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * A {@link BeehiveBlock} extension that fills its item after the vanilla beehive item.
 */
@SuppressWarnings("deprecation")
public class BlueprintBeehiveBlock extends BeehiveBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.BEEHIVE);

    public BlueprintBeehiveBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlueprintBlockEntityTypes.BEEHIVE.get().instantiate(pos, state);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return mirror == BlockMirror.NONE ? state : state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
