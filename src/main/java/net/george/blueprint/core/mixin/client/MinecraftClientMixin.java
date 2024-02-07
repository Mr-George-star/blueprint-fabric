package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.events.WorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable
    public ClientWorld world;

    @Inject(method = "joinWorld", at = @At("HEAD"))
    public void joinWorld(ClientWorld world, CallbackInfo ci) {
        if (this.world != null) {
            WorldEvents.UNLOAD.invoker().interact(this.world);
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    public void disconnect(Screen screen, CallbackInfo ci) {
        if (this.world != null) {
            WorldEvents.UNLOAD.invoker().interact(this.world);
        }
    }
}
