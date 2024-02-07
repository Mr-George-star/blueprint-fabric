package net.george.blueprint.core.mixin.client;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.george.blueprint.core.events.EntityViewRenderEvents;
import net.george.blueprint.core.events.RegisteredShadersCallback;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final
    private Map<String, Shader> shaders;

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", shift = At.Shift.AFTER, by = 1))
    public void renderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci, @Local Camera camera) {
        EntityViewRenderEvents.CAMERA_SETUP.invoker().interact((GameRenderer)(Object)this, camera, tickDelta, camera.getYaw(), camera.getPitch(), 0);
    }

    @Inject(method = "loadShaders", at = @At("TAIL"))
    public void loadShaders(ResourceManager manager, CallbackInfo ci) {
        List<Pair<Shader, Consumer<Shader>>> customShaders = Lists.newArrayList();
        RegisteredShadersCallback.EVENT.invoker().interact(manager, customShaders);

        customShaders.forEach(pair -> {
            Shader shader = pair.getFirst();
            this.shaders.put(shader.getName(), shader);
            pair.getSecond().accept(shader);
        });
    }
}
