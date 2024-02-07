package net.george.blueprint.core.mixin.extension;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.util.extension.AdvancementBuilderExtension;
import net.george.blueprint.core.util.extension.ServerAdvancementLoaderExtension;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementLoader.class)
public class ServerAdvancementLoaderMixin implements ServerAdvancementLoaderExtension {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow
    private AdvancementManager manager;
    @Shadow @Final
    private LootConditionManager conditionManager;

    @Unique
    private ICondition.IContext context = ICondition.IContext.EMPTY;

    @Override
    public ServerAdvancementLoader setContext(ICondition.IContext context) {
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
        Map<Identifier, Advancement.Builder> map2 = Maps.newHashMap();
        map.forEach((id, json) -> {
            try {
                JsonObject jsonObject = JsonHelper.asObject(json, "advancement");
                Advancement.Builder builder = AdvancementBuilderExtension.fromJson(jsonObject, new AdvancementEntityPredicateDeserializer(id, this.conditionManager), context);
                map2.put(id, builder);
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", id, var6.getMessage());
            }

        });
        AdvancementManager advancementManager = new AdvancementManager();
        advancementManager.load(map2);

        for (Advancement advancement : advancementManager.getRoots()) {
            if (advancement.getDisplay() != null) {
                AdvancementPositioner.arrangeForTree(advancement);
            }
        }

        this.manager = advancementManager;
    }
}
