package net.george.blueprint.core.other.tags;

import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.other.BlueprintCoordinationMods;
import net.george.blueprint.core.util.TagUtil;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlueprintBlockTags {
    public static final TagKey<Block> BOOKSHELVES = TagKey.of(Registry.BLOCK_KEY, new Identifier(Blueprint.MOD_ID, "bookshelves"));
    
    public static final TagKey<Block> LEAF_PILES = TagUtil.blockTag(BlueprintCoordinationMods.WOODWORKS.asString(), "leaf_piles");
    public static final TagKey<Block> HEDGES = TagUtil.blockTag(BlueprintCoordinationMods.QUARK.asString(), "hedges");
    public static final TagKey<Block> LADDERS = TagUtil.blockTag(BlueprintCoordinationMods.QUARK.asString(), "ladders");
    public static final TagKey<Block> VERTICAL_SLABS = TagUtil.blockTag(BlueprintCoordinationMods.QUARK.asString(), "vertical_slabs");
    public static final TagKey<Block> WOODEN_VERTICAL_SLABS = TagUtil.blockTag(BlueprintCoordinationMods.QUARK.asString(), "wooden_vertical_slabs");
}
