package net.george.blueprint.core.api.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.annotations.ConfigKey;
import net.george.blueprint.core.api.conditions.config.IConfigPredicate;
import net.george.blueprint.core.api.conditions.config.IConfigPredicateSerializer;
import net.george.blueprint.core.api.config.ForgeConfigSpec;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.api.recipe.condition.IConditionSerializer;
import net.george.blueprint.core.util.DataUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * A condition that checks against the values of config values annotated with {@link ConfigKey}.
 * <p>Uses the recipe system, but is also compatible with modifiers etc.</p>
 *
 * <p>To make your mod's config values compatible with it, annotate them with {@link ConfigKey} taking in the string
 * value that should be used to deserialize the field, then call {@link DataUtil#registerConfigCondition(String, Object...)}
 * in the mod setup, passing your mod id as the first parameter, and the config objects with the
 * {@link ForgeConfigSpec.ConfigValue} instances that should be checked against as the second.</p>
 *
 * <p>For a condition with type {@code "[modid]:config"}, it takes the arguments:
 * <ul>
 *   <li>{@code value}      - the name of the config value to check against, defined by its corresponding {@link ConfigKey} annotation.</li>
 *   <li>{@code predicates} - an array of JSON objects that deserialize to an {@link IConfigPredicate}, which prevent
 *                            the condition from passing if one of more of them return false. Optional if {@code value}
 *                            maps to a boolean {@link ForgeConfigSpec.ConfigValue}.</li>
 *   <li>{@code inverted} (optional)   - whether the condition should be inverted, so it will pass if {@code predicates} return false instead.</li>
 * </ul></p>
 *
 * @author abigailfails
 * @see DataUtil#registerConfigCondition(String, Object...)
 */
public class ConfigValueCondition implements ICondition {
    private final ForgeConfigSpec.ConfigValue<?> value;
    private final String valueId;
    private final Map<IConfigPredicate, Boolean> predicates;
    private final boolean inverted;
    private final Identifier identifier;

    public ConfigValueCondition(Identifier identifier, ForgeConfigSpec.ConfigValue<?> value, String valueId, Map<IConfigPredicate, Boolean> predicates, boolean inverted) {
        this.identifier = identifier;
        this.value = value;
        this.valueId = valueId;
        this.predicates = predicates;
        this.inverted = inverted;
    }

    @Override
    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public boolean test() {
        boolean returnValue;
        Map<IConfigPredicate, Boolean> predicates = this.predicates;
        ForgeConfigSpec.ConfigValue<?> value = this.value;
        if (predicates.size() > 0) {
            returnValue = predicates.keySet().stream().allMatch(c -> predicates.get(c) != c.test(value));
        } else if (value.get() instanceof Boolean bool) {
            returnValue = bool;
        } else {
            throw new IllegalStateException("Predicates required for non-boolean ConfigLootCondition, but none found");
        }
        return this.inverted != returnValue;
    }

    public static class Serializer implements IConditionSerializer<ConfigValueCondition> {
        public static final Hashtable<Identifier, IConfigPredicateSerializer<?>> CONFIG_PREDICATE_SERIALIZERS = new Hashtable<>();
        private final Map<String, ForgeConfigSpec.ConfigValue<?>> configValues;
        private final Identifier identifier;

        public Serializer(String modId, Map<String, ForgeConfigSpec.ConfigValue<?>> configValues) {
            this.identifier = new Identifier(modId, "config");
            this.configValues = configValues;
        }

        @Override
        public void write(JsonObject json, ConfigValueCondition value) {
            json.addProperty("value", value.valueId);
            if (!value.predicates.isEmpty()) {
                JsonArray predicates = new JsonArray();
                json.add("predicates", predicates);
                for (Map.Entry<IConfigPredicate, Boolean> predicatePair : value.predicates.entrySet()) {
                    IConfigPredicate predicate = predicatePair.getKey();
                    Identifier predicateId = predicate.getId();
                    JsonObject object = new JsonObject();
                    predicates.add(object);
                    object.addProperty("type", predicateId.toString());
                    CONFIG_PREDICATE_SERIALIZERS.get(predicateId).write(object, predicate);
                    object.addProperty("inverted", predicatePair.getValue());
                }
            }
            if (value.inverted) {
                json.addProperty("inverted", true);
            }
        }

        @Override
        public ConfigValueCondition read(JsonObject json) {
            if (!json.has("value")) {
                throw new JsonSyntaxException("Missing 'value', expected to find a string");
            }
            String name = JsonHelper.getString(json, "value");
            ForgeConfigSpec.ConfigValue<?> configValue = configValues.get(name);
            if (configValue == null) {
                throw new JsonSyntaxException("No config value of name '" + name + "' found");
            }
            Map<IConfigPredicate, Boolean> predicates = new HashMap<>();
            if (JsonHelper.hasElement(json, "predicates")) {
                for (JsonElement predicateElement : JsonHelper.getArray(json, "predicates")) {
                    if (!predicateElement.isJsonObject()) {
                        throw new JsonSyntaxException("Predicates must be an array of JsonObjects");
                    }
                    JsonObject predicateObject = predicateElement.getAsJsonObject();
                    Identifier type = new Identifier(JsonHelper.getString(predicateObject, "type"));
                    IConfigPredicateSerializer<?> serializer = CONFIG_PREDICATE_SERIALIZERS.get(type);
                    if (serializer == null) {
                        throw new JsonSyntaxException("Unknown predicate type: " + type);
                    }
                    predicates.put(serializer.read(predicateObject), predicateObject.has("inverted") && JsonHelper.getBoolean(predicateObject, "inverted"));
                }
            } else if (!(configValue.get() instanceof Boolean)) {
                throw new JsonSyntaxException("Missing 'predicates' for non-boolean config value '" + name + "', expected to find an array");
            }
            return new ConfigValueCondition(this.identifier, configValue, name, predicates, json.has("inverted") && JsonHelper.getBoolean(json, "inverted"));
        }

        @Override
        public Identifier getId() {
            return this.identifier;
        }
    }
}
