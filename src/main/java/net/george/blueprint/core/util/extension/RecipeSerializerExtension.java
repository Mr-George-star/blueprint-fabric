package net.george.blueprint.core.util.extension;

import com.google.gson.JsonObject;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

@SuppressWarnings("unused")
public interface RecipeSerializerExtension<T extends Recipe<?>> {
    private RecipeSerializer<T> self() {
        return (RecipeSerializer<T>)this;
    }

    default T read(Identifier id, JsonObject json, ICondition.IContext context) {
        return self().read(id, json);
    }
}
