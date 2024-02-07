package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class NotCondition implements ICondition {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "not_condition");
    private final ICondition child;

    public NotCondition(ICondition child) {
        this.child = child;
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
        return !child.test(context);
    }

    @Override
    public String toString() {
        return "!" + child;
    }

    public static class Serializer implements IConditionSerializer<NotCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, NotCondition value) {
            json.add("value", CraftingHelper.serialize(value.child));
        }

        @Override
        public NotCondition read(JsonObject json) {
            return new NotCondition(CraftingHelper.getCondition(JsonHelper.getObject(json, "value")));
        }

        @Override
        public Identifier getId() {
            return NotCondition.ID;
        }
    }
}