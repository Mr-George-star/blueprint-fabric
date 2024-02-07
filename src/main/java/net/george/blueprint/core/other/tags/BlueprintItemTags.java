package net.george.blueprint.core.other.tags;

import net.george.blueprint.core.other.BlueprintCoordinationMods;
import net.george.blueprint.core.util.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

public class BlueprintItemTags {
    public static final TagKey<Item> BOATABLE_CHESTS = TagUtil.itemTag(BlueprintCoordinationMods.QUARK.asString(), "boatable_chests");
    public static final TagKey<Item> LADDERS = TagUtil.itemTag(BlueprintCoordinationMods.QUARK.asString(), "ladders");
    public static final TagKey<Item> VERTICAL_SLABS = TagUtil.itemTag(BlueprintCoordinationMods.QUARK.asString(), "vertical_slabs");
    public static final TagKey<Item> WOODEN_VERTICAL_SLABS = TagUtil.itemTag(BlueprintCoordinationMods.QUARK.asString(), "wooden_vertical_slabs");
}
