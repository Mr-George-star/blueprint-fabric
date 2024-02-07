package net.george.blueprint.core.util.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.util.Map;

@SuppressWarnings("unused")
public interface RecipeManagerExtension {
    default RecipeManager self() {
        return (RecipeManager)this;
    }

    default RecipeManager setContext(ICondition.IContext context) {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }

    default void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, ICondition.IContext context) {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }

    static Recipe<?> deserialize(Identifier id, JsonObject json, ICondition.IContext context) {
        String string = JsonHelper.getString(json, "type");
        return Registry.RECIPE_SERIALIZER.getOrEmpty(new Identifier(string)).orElseThrow(() ->
                new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'")).read(id, json, context);
    }
}
