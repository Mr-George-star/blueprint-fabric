package net.george.blueprint.core.api.conditions.loot;

import com.google.gson.*;
import net.george.blueprint.core.api.conditions.ConfigValueCondition;
import net.george.blueprint.core.api.conditions.config.IConfigPredicate;
import net.george.blueprint.core.api.conditions.config.IConfigPredicateSerializer;
import net.george.blueprint.core.api.config.ForgeConfigSpec;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link LootCondition} implementation that is registered and functions exactly the same way as {@link ConfigValueCondition}, but for loot tables instead.
 *
 * @author abigailfails
 */
public class ConfigLootCondition implements LootCondition {
    private final ForgeConfigSpec.ConfigValue<?> value;
    private final String valueId;
    private final Map<IConfigPredicate, Boolean> predicates;
    private final boolean inverted;
    private final Identifier identifier;

    public ConfigLootCondition(Identifier identifier, ForgeConfigSpec.ConfigValue<?> value, String valueId, Map<IConfigPredicate, Boolean> predicates, boolean inverted) {
        this.identifier = identifier;
        this.value = value;
        this.valueId = valueId;
        this.predicates = predicates;
        this.inverted = inverted;
    }

    @Override
    public LootConditionType getType() {
        return Registry.LOOT_CONDITION_TYPE.get(this.identifier);
    }

    @Override
    public boolean test(LootContext context) {
        boolean returnValue;
        if (predicates.size() > 0) {
            returnValue = this.predicates.keySet().stream().allMatch(predicate -> this.predicates.get(predicate) != predicate.test(value));
        } else if (value.get() instanceof Boolean bool) {
            returnValue = bool;
        } else {
            throw new IllegalStateException("Predicates required for non-boolean ConfigLootCondition, but none found");
        }
        return this.inverted != returnValue;
    }

    public static class ConfigSerializer implements JsonSerializer<ConfigLootCondition> {
        private final Map<String, ForgeConfigSpec.ConfigValue<?>> configValues;
        private final Identifier identifier;

        public ConfigSerializer(String modId, Map<String, ForgeConfigSpec.ConfigValue<?>> configValues) {
            this.identifier = new Identifier(modId, "config");
            this.configValues = configValues;
        }

        @Override
        public void toJson(JsonObject json, ConfigLootCondition value, JsonSerializationContext context) {
            json.addProperty("value", value.valueId);
            if (!value.predicates.isEmpty()) {
                JsonArray predicates = new JsonArray();
                for (Map.Entry<IConfigPredicate, Boolean> predicatePair : value.predicates.entrySet()) {
                    IConfigPredicate predicate = predicatePair.getKey();
                    Identifier predicateId = predicate.getId();
                    JsonObject object = new JsonObject();
                    object.addProperty("type", predicateId.toString());
                    ConfigValueCondition.Serializer.CONFIG_PREDICATE_SERIALIZERS.get(predicateId).write(object, predicate);
                    object.addProperty("inverted", predicatePair.getValue());
                    predicates.add(object);
                }
                json.add("predicates", predicates);
            }
            if (value.inverted){
                json.addProperty("inverted", true);
            }
        }

        @Override
        public ConfigLootCondition fromJson(JsonObject json, JsonDeserializationContext context) {
            if (!json.has("value")) {
                throw new JsonSyntaxException("Missing 'value', expected to find a string");
            }
            String name = JsonHelper.getString(json, "value");
            ForgeConfigSpec.ConfigValue<?> configValue = this.configValues.get(name);
            if (configValue == null)
                throw new JsonSyntaxException("No config value of name '" + name + "' found");
            Map<IConfigPredicate, Boolean> predicates = new HashMap<>();
            if (JsonHelper.hasElement(json, "predicates")) {
                for (JsonElement predicateElement : JsonHelper.getArray(json, "predicates")) {
                    if (!predicateElement.isJsonObject()) {
                        throw new JsonSyntaxException("Predicates must be an array of JsonObjects");
                    }
                    JsonObject predicateObject = predicateElement.getAsJsonObject();
                    Identifier type = new Identifier(JsonHelper.getString(predicateObject, "type"));
                    IConfigPredicateSerializer<?> serializer = ConfigValueCondition.Serializer.CONFIG_PREDICATE_SERIALIZERS.get(type);
                    if (serializer == null)
                        throw new JsonSyntaxException("Unknown predicate type: " + type);
                    predicates.put(serializer.read(predicateObject), predicateObject.has("inverted") && JsonHelper.getBoolean(predicateObject, "inverted"));
                }
            } else if (!(configValue.get() instanceof Boolean)) {
                throw new JsonSyntaxException("Missing 'predicates' for non-boolean config value '" + name + "', expected to find an array");
            }
            return new ConfigLootCondition(this.identifier, configValue, name, predicates, json.has("inverted") && JsonHelper.getBoolean(json, "inverted"));
        }
    }
}
