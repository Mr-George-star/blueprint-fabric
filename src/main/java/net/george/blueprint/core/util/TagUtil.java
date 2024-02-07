package net.george.blueprint.core.util;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class containing some simple methods for making tags.
 *
 * @author bageldotjpg
 */
@ApiStatus.NonExtendable
@SuppressWarnings("unused")
public final class TagUtil {
    public static TagKey<Block> blockTag(String modid, String name) {
        return TagKey.of(Registry.BLOCK_KEY, new Identifier(modid, name));
    }

    public static TagKey<Item> itemTag(String modid, String name) {
        return TagKey.of(Registry.ITEM_KEY, new Identifier(modid, name));
    }

    public static TagKey<EntityType<?>> entityTypeTag(String modid, String name) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(modid, name));
    }

    public static TagKey<Enchantment> enchantmentTag(String modid, String name) {
        return TagKey.of(Registry.ENCHANTMENT_KEY, new Identifier(modid, name));
    }

    public static TagKey<Potion> potionTag(String modid, String name) {
        return TagKey.of(Registry.POTION_KEY, new Identifier(modid, name));
    }

    public static TagKey<BlockEntityType<?>> blockEntityTypeTag(String modid, String name) {
        return TagKey.of(Registry.BLOCK_ENTITY_TYPE_KEY, new Identifier(modid, name));
    }

    public static TagKey<StatusEffect> mobEffectTag(String modid, String name) {
        return TagKey.of(Registry.MOB_EFFECT_KEY, new Identifier(modid, name));
    }

    public static TagKey<Biome> biomeTag(String modid, String name) {
        return TagKey.of(Registry.BIOME_KEY, new Identifier(modid, name));
    }

    public static TagKey<World> dimensionTag(String modid, String name) {
        return TagKey.of(Registry.WORLD_KEY, new Identifier(modid, name));
    }

    public static TagKey<DimensionType> dimensionTypeTag(String modid, String name) {
        return TagKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(modid, name));
    }

    public static TagKey<ConfiguredFeature<?, ?>> configuredFeatureTag(String modid, String name) {
        return TagKey.of(Registry.CONFIGURED_FEATURE_KEY, new Identifier(modid, name));
    }

    public static TagKey<PlacedFeature> placedFeatureTag(String modid, String name) {
        return TagKey.of(Registry.PLACED_FEATURE_KEY, new Identifier(modid, name));
    }

    public static TagKey<ConfiguredStructureFeature<?, ?>> configuredStructureTag(String modid, String name) {
        return TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, new Identifier(modid, name));
    }

    public static TagKey<ConfiguredCarver<?>> configuredCarverTag(String modid, String name) {
        return TagKey.of(Registry.CONFIGURED_CARVER_KEY, new Identifier(modid, name));
    }
}
