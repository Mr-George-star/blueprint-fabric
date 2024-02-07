package net.george.blueprint.core.mixin.extension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.util.extension.RecipeManagerExtension;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements RecipeManagerExtension {
    @Shadow
    private boolean errored;
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow
    private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
    @Shadow
    private Map<Identifier, Recipe<?>> recipesById;

    @Shadow protected abstract void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler);

    @Unique
    private ICondition.IContext context = ICondition.IContext.EMPTY;

    @Override
    public RecipeManager setContext(ICondition.IContext context) {
        this.context = context;
        return self();
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"), cancellable = true)
    public void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        if (this.context != ICondition.IContext.EMPTY) {
            apply(map, resourceManager, profiler, this.context);
            ci.cancel();
        }
    }

    @Override
    public void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, ICondition.IContext context) {
        this.errored = false;
        Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> map2 = Maps.newHashMap();
        ImmutableMap.Builder<Identifier, Recipe<?>> builder = ImmutableMap.builder();

        for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier identifier = entry.getKey();
            if (identifier.getPath().startsWith("_")) {
                continue;
            }

            try {
                if (entry.getValue().isJsonObject() && !CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", context)) {
                    LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", identifier);
                    continue;
                }
                Recipe<?> recipe = RecipeManagerExtension.deserialize(identifier, JsonHelper.asObject(entry.getValue(), "top element"), context);
                if (recipe == null) {
                    LOGGER.info("Skipping loading recipe {} as it's serializer returned null", identifier);
                    continue;
                }

                map2.computeIfAbsent(recipe.getType(), (recipeType) -> ImmutableMap.builder()).put(identifier, recipe);
                builder.put(identifier, recipe);
            } catch (IllegalArgumentException | JsonParseException var10) {
                LOGGER.error("Parsing error loading recipe {}", identifier, var10);
            }
        }

        this.recipes = map2.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
        this.recipesById = builder.build();
        LOGGER.info("Loaded {} recipes", map2.size());
    }

    @Inject(method = "deserialize", at = @At("HEAD"), cancellable = true)
    private static void deserialize(Identifier id, JsonObject json, CallbackInfoReturnable<Recipe<?>> cir) {
        cir.setReturnValue(RecipeManagerExtension.deserialize(id, json, ICondition.IContext.EMPTY));
    }
}
