package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.events.PlayerLoginCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerNetworkHandlerMixin {
    @Shadow @Final
    private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;init()V", shift = At.Shift.AFTER, by = 1))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        PlayerLoginCallback.EVENT.invoker().interact(this.client.interactionManager, this.client.player, this.client.getNetworkHandler().getConnection());
    }
}
