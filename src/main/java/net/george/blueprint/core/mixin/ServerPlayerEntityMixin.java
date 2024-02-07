package net.george.blueprint.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.george.blueprint.core.events.PlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(at = @At("TAIL"), method = "copyFrom")
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        PlayerEvents.CLONE.invoker().interact((ServerPlayerEntity)(Object)this, oldPlayer, alive);
    }

    @Inject(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir, @Local RegistryKey<World> registryKey) {
        PlayerEvents.CHANGE_DIMENSION.invoker().interact((ServerPlayerEntity)(Object)this, registryKey, destination.getRegistryKey());
    }
}
