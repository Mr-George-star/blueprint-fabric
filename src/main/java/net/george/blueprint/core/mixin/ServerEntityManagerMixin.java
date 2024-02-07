package net.george.blueprint.core.mixin;

import net.george.blueprint.core.events.EntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerEntityManager.class)
public abstract class ServerEntityManagerMixin<T extends EntityLike> {
    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
    public void addEntity(T entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Entity targetEntity) {
            EntityEvents.JOIN_WORLD.invoker().interact(targetEntity, targetEntity.world, existing);
        }
    }
}
