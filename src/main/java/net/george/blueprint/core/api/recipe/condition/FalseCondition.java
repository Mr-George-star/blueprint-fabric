package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;

public final class FalseCondition implements ICondition {
    public static final FalseCondition INSTANCE = new FalseCondition();
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "false_condition");

    public FalseCondition() {
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test() {
        return false;
    }

    @Override
    public String toString() {
        return "false";
    }

    public static class Serializer implements IConditionSerializer<FalseCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, FalseCondition value) {
        }

        @Override
        public FalseCondition read(JsonObject json) {
            return FalseCondition.INSTANCE;
        }

        @Override
        public Identifier getId() {
            return FalseCondition.ID;
        }
    }
}

