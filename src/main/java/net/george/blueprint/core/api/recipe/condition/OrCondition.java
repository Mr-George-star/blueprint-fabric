package net.george.blueprint.core.api.recipe.condition;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class OrCondition implements ICondition {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "or_condition");
    private final ICondition[] children;

    public OrCondition(ICondition... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values must not be empty");
        }

        for (ICondition child : values) {
            if (child == null) {
                throw new IllegalArgumentException("Value must not be null");
            }
        }
        this.children = values;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test() {
        return test(IContext.EMPTY);
    }

    @Override
    public boolean test(IContext context) {
        for (ICondition child : children) {
            if (child.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return Joiner.on(" || ").join(children);
    }

    public static class Serializer implements IConditionSerializer<OrCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, OrCondition value) {
            JsonArray values = new JsonArray();
            for (ICondition child : value.children) {
                values.add(CraftingHelper.serialize(child));
            }
            json.add("values", values);
        }

        @Override
        public OrCondition read(JsonObject json) {
            List<ICondition> children = new ArrayList<>();
            for (JsonElement j : JsonHelper.getArray(json, "values")) {
                if (!j.isJsonObject()) {
                    throw new JsonSyntaxException("Or condition values must be an array of JsonObjects");
                }
                children.add(CraftingHelper.getCondition(j.getAsJsonObject()));
            }
            return new OrCondition(children.toArray(new ICondition[0]));
        }

        @Override
        public Identifier getId() {
            return OrCondition.ID;
        }
    }
}