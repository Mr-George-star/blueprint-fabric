package net.george.blueprint.core.mixin;

import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.core.util.NetworkUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final
    private Entity entity;

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;velocityDirty:Z", ordinal = 2, shift = At.Shift.AFTER))
    public void tickVelocityDirty(CallbackInfo ci) {
        updateEntityData(this.entity);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        Entity entity = this.entity;
        IDataManager dataManager = (IDataManager)entity;
        if (dataManager.isDirty()) {
            updateEntityData(entity);
        }
    }

    private static void updateEntityData(Entity entity) {
        IDataManager dataManager = (IDataManager)entity;
        Set<IDataManager.DataEntry<?>> entries = dataManager.getDirtyEntries();
        if (!entries.isEmpty()) {
            if (entity instanceof ServerPlayerEntity) {
                NetworkUtil.updateTrackedData((ServerPlayerEntity) entity, entity.getId(), entries);
            }

            NetworkUtil.updateTrackedData(entity, entries);
        }

        dataManager.clean();
    }
}
