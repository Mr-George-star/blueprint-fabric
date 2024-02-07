package net.george.blueprint.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.george.blueprint.core.api.recipe.condition.ConditionContext;
import net.george.blueprint.core.events.ResourceReloadCallback;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Unit;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    private static CompletableFuture<Unit> COMPLETED_UNIT;

    @Inject(method = "reload", at = @At("RETURN"), cancellable = true)
    private static void reload(ResourceManager manager, DynamicRegistryManager.Immutable dynamicRegistryManager,
                               CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel,
                               Executor prepareExecutor, Executor applyExecutor,
                               CallbackInfoReturnable<CompletableFuture<DataPackContents>> cir, @Local DataPackContents dataPackContents) {
        List<ResourceReloader> reloaders = new ArrayList<>(dataPackContents.getContents());
        reloaders.addAll(ResourceReloadCallback.EVENT.invoker().interact(dataPackContents, new ArrayList<>()));
        cir.setReturnValue(SimpleResourceReload.start(manager, reloaders, prepareExecutor, applyExecutor, COMPLETED_UNIT,
                LOGGER.isDebugEnabled()).whenComplete().thenApply((object) -> dataPackContents));
    }
}
