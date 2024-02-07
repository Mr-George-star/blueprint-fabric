package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ModLoadedCondition implements ICondition {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "mod_loaded_condition");
    private final String modid;

    public ModLoadedCondition(String modid) {
        this.modid = modid;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test() {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public String toString() {
        return "mod_loaded(\"" + modid + "\")";
    }

    public static class Serializer implements IConditionSerializer<ModLoadedCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ModLoadedCondition value) {
            json.addProperty("modid", value.modid);
        }

        @Override
        public ModLoadedCondition read(JsonObject json) {
            return new ModLoadedCondition(JsonHelper.getString(json, "modid"));
        }

        @Override
        public Identifier getId()
        {
            return ModLoadedCondition.ID;
        }
    }
}