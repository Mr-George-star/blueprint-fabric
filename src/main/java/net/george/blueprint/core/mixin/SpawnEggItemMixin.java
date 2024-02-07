package net.george.blueprint.core.mixin;

import net.george.blueprint.core.util.item.filling.AlphabeticalItemCategoryFiller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;

/**
 * This fixes incompatibility issues that occur when other modded spawn eggs are not sorted alphabetically.
 * <p>Also technically a feature.</p>
 */
@Mixin(SpawnEggItem.class)
public final class SpawnEggItemMixin extends Item {
    private static final AlphabeticalItemCategoryFiller FILLER = AlphabeticalItemCategoryFiller.forClass(SpawnEggItem.class);

    private SpawnEggItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {
            Identifier name = Registry.ITEM.getId(this);
            if (!name.getNamespace().equals("minecraft") && (group == ItemGroup.MISC || group == ItemGroup.SEARCH)) {
                FILLER.fillItem(this, group, stacks);
            } else {
                super.appendStacks(group, stacks);
            }
        }
    }
}
