package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.events.EntityRendererEvents;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<?>> renderers;
    @Shadow
    private Map<String, EntityRenderer<? extends PlayerEntity>> modelRenderers;

    @Inject(method = "reload", at = @At("TAIL"))
    public void reload(ResourceManager manager, CallbackInfo ci) {
        EntityRendererEvents.ADD_LAYERS.invoker().interact(this.renderers, this.modelRenderers);
    }
}
