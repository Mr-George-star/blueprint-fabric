package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class TagEmptyCondition implements ICondition {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "tag_empty_condition");
    private final TagKey<Item> tag;

    public TagEmptyCondition(String location) {
        this(new Identifier(location));
    }

    public TagEmptyCondition(String namespace, String path) {
        this(new Identifier(namespace, path));
    }

    public TagEmptyCondition(Identifier tag) {
        this.tag = TagKey.of(Registry.ITEM_KEY, tag);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return context.getTag(tag).values().isEmpty();
    }

    @Override
    public boolean test() {
        return test(IContext.EMPTY);
    }

    @Override
    public String toString() {
        return "tag_empty(\"" + tag.id() + "\")";
    }

    public static class Serializer implements IConditionSerializer<TagEmptyCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, TagEmptyCondition value) {
            json.addProperty("tag", value.tag.id().toString());
        }

        @Override
        public TagEmptyCondition read(JsonObject json) {
            return new TagEmptyCondition(new Identifier(JsonHelper.getString(json, "tag")));
        }

        @Override
        public Identifier getId() {
            return TagEmptyCondition.ID;
        }
    }
}