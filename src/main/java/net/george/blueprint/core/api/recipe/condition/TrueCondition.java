package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;

public final class TrueCondition implements ICondition {
    public static final TrueCondition INSTANCE = new TrueCondition();
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "true_condition");

    public TrueCondition() {
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test() {
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

    public static class Serializer implements IConditionSerializer<TrueCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, TrueCondition value) {
        }

        @Override
        public TrueCondition read(JsonObject json) {
            return TrueCondition.INSTANCE;
        }

        @Override
        public Identifier getId() {
            return TrueCondition.ID;
        }
    }
}