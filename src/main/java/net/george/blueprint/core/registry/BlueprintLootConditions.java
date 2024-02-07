package net.george.blueprint.core.registry;

import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.conditions.loot.RaidCheckCondition;
import net.george.blueprint.core.api.conditions.loot.RandomDifficultyChanceCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Registry class for Blueprint's built-in loot conditions.
 *
 * <p>These conditions can be used by mods and datapacks.</p>
 *
 * @author abigailfails
 */
public class BlueprintLootConditions {
    public static final Identifier RANDOM_DIFFICULTY_CHANCE = new Identifier(Blueprint.MOD_ID, "random_difficulty_chance");
    public static final Identifier RAID_CHECK = new Identifier(Blueprint.MOD_ID, "raid_check");

    public static void register() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, RANDOM_DIFFICULTY_CHANCE, new LootConditionType(new RandomDifficultyChanceCondition.RandomDifficultyChanceSerializer()));
        Registry.register(Registry.LOOT_CONDITION_TYPE, RAID_CHECK, new LootConditionType(new RaidCheckCondition.RaidCheckSerializer()));
    }
}
