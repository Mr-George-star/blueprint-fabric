package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class ItemExistsCondition implements ICondition {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "item_exists_condition");
    private final Identifier item;

    public ItemExistsCondition(String location) {
        this(new Identifier(location));
    }

    public ItemExistsCondition(String namespace, String path) {
        this(new Identifier(namespace, path));
    }

    public ItemExistsCondition(Identifier item) {
        this.item = item;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test() {
        return Registry.ITEM.containsId(this.item);
    }

    @Override
    public String toString() {
        return "item_exists(\"" + item + "\")";
    }

    public static class Serializer implements IConditionSerializer<ItemExistsCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ItemExistsCondition value) {
            json.addProperty("item", value.item.toString());
        }

        @Override
        public ItemExistsCondition read(JsonObject json) {
            return new ItemExistsCondition(new Identifier(JsonHelper.getString(json, "item")));
        }

        @Override
        public Identifier getId() {
            return ItemExistsCondition.ID;
        }
    }
}