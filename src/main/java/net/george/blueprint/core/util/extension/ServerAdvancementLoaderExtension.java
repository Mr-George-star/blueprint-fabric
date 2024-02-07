package net.george.blueprint.core.util.extension;

import com.google.gson.JsonElement;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

@SuppressWarnings("unused")
public interface ServerAdvancementLoaderExtension {
    default ServerAdvancementLoader self() {
        return (ServerAdvancementLoader)this;
    }

    default ServerAdvancementLoader setContext(ICondition.IContext context) {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }

    default void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, ICondition.IContext context) {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }
}
