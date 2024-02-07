package net.george.blueprint.core.api.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.george.blueprint.core.api.recipe.condition.AndCondition;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.api.recipe.condition.IConditionSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A special version of the {@link AndCondition} that stops reading if a false condition is met.
 * <p>This is useful for testing another condition only if the former conditions are met.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class BlueprintAndCondition implements ICondition {
    private final Identifier identifier;
    private final List<ICondition> children;

    public BlueprintAndCondition(Identifier location, List<ICondition> children) {
        this.identifier = location;
        this.children = children;
    }

    @Override
    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public boolean test() {
        return !this.children.isEmpty();
    }

    public static class Serializer implements IConditionSerializer<BlueprintAndCondition> {
        private final Identifier identifier;

        public Serializer() {
            this.identifier = new Identifier(Blueprint.MOD_ID, "and");
        }

        @Override
        public void write(JsonObject json, BlueprintAndCondition value) {
            JsonArray values = new JsonArray();
            for (ICondition child : value.children) {
                values.add(CraftingHelper.serialize(child));
            }
            json.add("values", values);
        }

        @Override
        public BlueprintAndCondition read(JsonObject json) {
            List<ICondition> children = new ArrayList<>();
            for (JsonElement elements : JsonHelper.getArray(json, "values")) {
                if (!elements.isJsonObject()) {
                    throw new JsonSyntaxException("And condition values must be an array of JsonObjects");
                }
                ICondition condition = CraftingHelper.getCondition(elements.getAsJsonObject());
                if (!condition.test()) {
                    children.clear();
                    break;
                } else {
                    children.add(condition);
                }
            }
            return new BlueprintAndCondition(this.identifier, children);
        }

        @Override
        public Identifier getId() {
            return this.identifier;
        }
    }
}
