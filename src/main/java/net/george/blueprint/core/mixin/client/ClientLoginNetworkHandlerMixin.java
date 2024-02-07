package net.george.blueprint.core.mixin.client;

import net.george.blueprint.common.network.NetworkHooks;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    @Shadow @Final
    private ClientConnection connection;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setState(Lnet/minecraft/network/NetworkState;)V", shift = At.Shift.AFTER), method = "onSuccess")
    public void onSuccess(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        NetworkHooks.handleClientLoginSuccess(this.connection);
    }
}
