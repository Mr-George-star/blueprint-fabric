package net.george.blueprint.common.block.sign;

import net.george.blueprint.core.registry.BlueprintBlockEntityTypes;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SignType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

/**
 * A {@link SignBlock} extension used for Blueprint's standing signs.
 */
public class BlueprintStandingSignBlock extends SignBlock implements IBlueprintSign {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WARPED_SIGN);

    public BlueprintStandingSignBlock(Settings settings, SignType type) {
        super(settings, type);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlueprintBlockEntityTypes.SIGN.get().instantiate(pos, state);
    }
}
