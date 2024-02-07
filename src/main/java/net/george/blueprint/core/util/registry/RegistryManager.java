package net.george.blueprint.core.util.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class RegistryManager {
    private static final BiMap<Registry<?>, Class<?>> registries = HashBiMap.create();

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(Class<T> registryClass) {
        return (Registry<T>) getRegistryMap().inverse().get(registryClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getRegistryClass(Registry<T> registry) {
        return (Class<T>) getRegistryMap().get(registry);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getRegistryClass(RegistryKey<? extends Registry<?>> key) {
        Registry<?> registry = Registry.REGISTRIES.get(key.getValue());
        return (Class<T>) getRegistryClass(registry);
    }

    public static BiMap<Registry<?>, Class<?>> getRegistryMap() {
        if (registries.isEmpty()) {
            createDefaultRegistries();
        }
        return registries;
    }

    public static <T> void addRegistry(Registry<T> registry, Class<T> type) {
        registries.put(registry, type);
    }

    public static void forRegistries(BiConsumer<? super Registry<?>, ? super Class<?>> consumer) {
        registries.forEach(consumer);
    }
    
    private static void createDefaultRegistries() {
        registries.put(Registry.GAME_EVENT, GameEvent.class);
        registries.put(Registry.SOUND_EVENT, SoundEvent.class);
        registries.put(Registry.FLUID, Fluid.class);
        registries.put(Registry.STATUS_EFFECT, StatusEffect.class);
        registries.put(Registry.BLOCK, Block.class);
        registries.put(Registry.ENCHANTMENT, Enchantment.class);
        registries.put(Registry.ENTITY_TYPE, EntityType.class);
        registries.put(Registry.ITEM, Item.class);
        registries.put(Registry.POTION, Potion.class);
        registries.put(Registry.PARTICLE_TYPE, ParticleType.class);
        registries.put(Registry.BLOCK_ENTITY_TYPE, BlockEntityType.class);
        registries.put(Registry.PAINTING_MOTIVE, PaintingMotive.class);
        registries.put(Registry.CUSTOM_STAT, Stat.class);
        registries.put(Registry.CHUNK_STATUS, ChunkStatus.class);
        registries.put(Registry.RULE_TEST, RuleTestType.class);
        registries.put(Registry.POS_RULE_TEST, PosRuleTestType.class);
        registries.put(Registry.SCREEN_HANDLER, ScreenHandler.class);
        registries.put(Registry.RECIPE_TYPE, RecipeType.class);
        registries.put(Registry.RECIPE_SERIALIZER, RecipeSerializer.class);
        registries.put(Registry.ATTRIBUTE, EntityAttribute.class);
        registries.put(Registry.POSITION_SOURCE_TYPE, PositionSourceType.class);
        registries.put(Registry.STAT_TYPE, StatType.class);
        registries.put(Registry.VILLAGER_TYPE, VillagerType.class);
        registries.put(Registry.VILLAGER_PROFESSION, VillagerProfession.class);
        registries.put(Registry.POINT_OF_INTEREST_TYPE, PointOfInterestType.class);
        registries.put(Registry.MEMORY_MODULE_TYPE, MemoryModuleType.class);
        registries.put(Registry.SENSOR_TYPE, SensorType.class);
        registries.put(Registry.SCHEDULE, Schedule.class);
        registries.put(Registry.ACTIVITY, Activity.class);
        registries.put(Registry.LOOT_POOL_ENTRY_TYPE, LootPoolEntryType.class);
        registries.put(Registry.LOOT_FUNCTION_TYPE, LootFunctionType.class);
        registries.put(Registry.LOOT_CONDITION_TYPE, LootConditionType.class);
        registries.put(Registry.LOOT_NUMBER_PROVIDER_TYPE, LootNumberProviderType.class);
        registries.put(Registry.LOOT_NBT_PROVIDER_TYPE, LootNbtProviderType.class);
        registries.put(Registry.LOOT_SCORE_PROVIDER_TYPE, LootScoreProviderType.class);
        registries.put(Registry.FLOAT_PROVIDER_TYPE, FloatProviderType.class);
        registries.put(Registry.INT_PROVIDER_TYPE, IntProviderType.class);
        registries.put(Registry.HEIGHT_PROVIDER_TYPE, HeightProviderType.class);
        registries.put(Registry.BLOCK_PREDICATE_TYPE, BlockPredicateType.class);
        registries.put(Registry.CARVER, Carver.class);
        registries.put(Registry.FEATURE, Feature.class);
        registries.put(Registry.STRUCTURE_FEATURE, StructureFeature.class);
        registries.put(Registry.STRUCTURE_PLACEMENT, StructurePlacement.class);
        registries.put(Registry.STRUCTURE_PIECE, StructurePiece.class);
        registries.put(Registry.PLACEMENT_MODIFIER_TYPE, PlacementModifierType.class);
        registries.put(Registry.BLOCK_STATE_PROVIDER_TYPE, BlockStateProviderType.class);
        registries.put(Registry.FOLIAGE_PLACER_TYPE, FoliagePlacerType.class);
        registries.put(Registry.TRUNK_PLACER_TYPE, TrunkPlacerType.class);
        registries.put(Registry.TREE_DECORATOR_TYPE, TreeDecoratorType.class);
        registries.put(Registry.FEATURE_SIZE_TYPE, FeatureSizeType.class);
        registries.put(Registry.BIOME_SOURCE, BiomeSource.class);
        registries.put(Registry.CHUNK_GENERATOR, ChunkGenerator.class);
        registries.put(Registry.MATERIAL_CONDITION, MaterialRules.MaterialCondition.class);
        registries.put(Registry.MATERIAL_RULE, MaterialRules.MaterialRule.class);
        registries.put(Registry.DENSITY_FUNCTION_TYPE, DensityFunction.class);
        registries.put(Registry.STRUCTURE_PROCESSOR, StructureProcessor.class);
        registries.put(Registry.STRUCTURE_POOL_ELEMENT, StructurePoolElement.class);

        registries.put(BuiltinRegistries.CONFIGURED_CARVER, ConfiguredCarver.class);
        registries.put(BuiltinRegistries.CONFIGURED_FEATURE, ConfiguredFeature.class);
        registries.put(BuiltinRegistries.PLACED_FEATURE, PlacedFeature.class);
        registries.put(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, ConfiguredStructureFeature.class);
        registries.put(BuiltinRegistries.STRUCTURE_SET, StructureSet.class);
        registries.put(BuiltinRegistries.STRUCTURE_PROCESSOR_LIST, StructureProcessorList.class);
        registries.put(BuiltinRegistries.STRUCTURE_POOL, StructurePool.class);
        registries.put(BuiltinRegistries.BIOME, Biome.class);
        registries.put(BuiltinRegistries.NOISE_PARAMETERS, DoublePerlinNoiseSampler.NoiseParameters.class);
        registries.put(BuiltinRegistries.DENSITY_FUNCTION, DensityFunction.class);
        registries.put(BuiltinRegistries.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettings.class);
    }
}
