package net.george.blueprint.common.block;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Supplier;

/**
 * A {@link FlowerBlock} extension that stores information about the flower's stew effect and fills its item after the latest vanilla flower item.
 */
public class BlueprintFlowerBlock extends FlowerBlock {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.WITHER_ROSE);
    private final Supplier<StatusEffect> stewEffect;
    private final int stewEffectDuration;

    public BlueprintFlowerBlock(Supplier<StatusEffect> stewEffect, int stewEffectDuration, Settings settings) {
        super(StatusEffects.WEAKNESS, stewEffectDuration, settings);
        this.stewEffect = stewEffect;
        this.stewEffectDuration = stewEffectDuration;
    }

    @Override
    public StatusEffect getEffectInStew() {
        return this.stewEffect.get();
    }

    @Override
    public int getEffectInStewDuration() {
        return this.getEffectInStew().isInstant() ? this.stewEffectDuration : this.stewEffectDuration * 20;
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this.asItem(), group, stacks);
    }
}
