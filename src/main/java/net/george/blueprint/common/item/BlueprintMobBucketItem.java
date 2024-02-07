package net.george.blueprint.common.item;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Supplier;

/**
 * A {@link EntityBucketItem} extension that fills itself after the latest vanilla fish bucket item.
 */
public class BlueprintMobBucketItem extends EntityBucketItem {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.TROPICAL_FISH_BUCKET);

    public BlueprintMobBucketItem(Supplier<? extends EntityType<?>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, Settings settings) {
        super(entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this, group, stacks);
    }
}
