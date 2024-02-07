package net.george.blueprint.common.item;

import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Supplier;

/**
 * A {@link MusicDiscItem} extension that fills itself after the latest vanilla music disc item.
 */
public class BlueprintRecordItem extends MusicDiscItem {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.MUSIC_DISC_OTHERSIDE);

    public BlueprintRecordItem(int comparatorValue, Supplier<SoundEvent> sound, Settings builder) {
        super(comparatorValue, sound.get(), builder);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this, group, stacks);
    }
}
