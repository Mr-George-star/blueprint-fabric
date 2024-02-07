package net.george.blueprint.core.util;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public class ItemUtil {
    @Nullable
    public static Identifier getItemId(Item item) {
        return Registry.ITEM.getId(item) == Registry.ITEM.getDefaultId() ? null : Registry.ITEM.getId(item);
    }
}
