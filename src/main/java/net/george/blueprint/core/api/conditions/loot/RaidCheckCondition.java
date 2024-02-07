package net.george.blueprint.core.api.conditions.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.george.blueprint.core.registry.BlueprintLootConditions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.registry.Registry;

/**
 * A {@link LootCondition} implementation that passes if there is a raid at the entity's position.
 *
 * <p>Arguments:
 * <ul>
 *   <li>{@code inverted} (optional) - whether the condition should be inverted, so it will pass if there is not a raid instead.</li>
 * </ul></p>
 *
 * @author abigailfails
 */
public class RaidCheckCondition implements LootCondition {
    private final boolean inverted;

    public RaidCheckCondition(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public LootConditionType getType() {
        return Registry.LOOT_CONDITION_TYPE.get(BlueprintLootConditions.RAID_CHECK);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.requireParameter(LootContextParameters.THIS_ENTITY);
        return this.inverted != (entity != null && lootContext.getWorld().getRaidAt(entity.getBlockPos()) != null);
    }

    public static class RaidCheckSerializer implements JsonSerializer<RaidCheckCondition> {
        @Override
        public void toJson(JsonObject json, RaidCheckCondition object, JsonSerializationContext context) {
            if (object.inverted)
                json.addProperty("inverted", true);
        }

        @Override
        public RaidCheckCondition fromJson(JsonObject json, JsonDeserializationContext context) {
            return new RaidCheckCondition(JsonHelper.getBoolean(json, "inverted", false));
        }
    }
}
