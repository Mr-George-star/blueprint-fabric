package net.george.blueprint.core.mixin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.common.world.storage.tracking.SyncType;
import net.george.blueprint.common.world.storage.tracking.TrackedData;
import net.george.blueprint.common.world.storage.tracking.TrackedDataManager;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.endimator.Endimatable;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements IDataManager, Endimatable {
    @Shadow
    public World world;
    @Shadow
    private Vec3d pos;
    private Map<TrackedData<?>, IDataManager.DataEntry<?>> dataMap = Maps.newHashMap();
    private boolean dirty = false;
    private final EndimatedState endimatedState = new EndimatedState(this);

    @Shadow
    public abstract BlockPos getLandingPos();

    @SuppressWarnings("unchecked")
    public <T> void setValue(TrackedData<T> trackedData, T value) {
        IDataManager.DataEntry<T> entry = (IDataManager.DataEntry<T>)this.dataMap.computeIfAbsent(trackedData, IDataManager.DataEntry::new);
        if (!entry.getValue().equals(value)) {
            boolean dirty = !this.world.isClient && entry.getTrackedData().getSyncType() != SyncType.NOPE;
            entry.setValue(value, dirty);
            this.dirty = dirty;
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(TrackedData<T> trackedData) {
        return ((IDataManager.DataEntry<T>)this.dataMap.computeIfAbsent(trackedData, IDataManager.DataEntry::new)).getValue();
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void clean() {
        this.dataMap.values().forEach(IDataManager.DataEntry::clean);
        this.dirty = false;
    }

    public void setDataMap(Map<TrackedData<?>, IDataManager.DataEntry<?>> dataMap) {
        this.dirty = true;
        this.dataMap = dataMap;
    }

    public Map<TrackedData<?>, IDataManager.DataEntry<?>> getDataMap() {
        return this.dataMap;
    }

    public Set<IDataManager.DataEntry<?>> getDirtyEntries() {
        Set<IDataManager.DataEntry<?>> dirtyEntries = Sets.newHashSet();

        for (DataEntry<?> dataEntry : this.dataMap.values()) {
            if (dataEntry.isDirty() && dataEntry.getTrackedData().getSyncType() != SyncType.NOPE) {
                dirtyEntries.add(dataEntry);
            }
        }

        return dirtyEntries;
    }

    public Set<IDataManager.DataEntry<?>> getEntries(boolean syncToAll) {
        Set<IDataManager.DataEntry<?>> dirtyEntries = Sets.newHashSet();
        Iterator<?> var3 = this.dataMap.values().iterator();

        while(true) {
            IDataManager.DataEntry<?> entry;
            while(true) {
                if (!var3.hasNext()) {
                    return dirtyEntries;
                }

                entry = (IDataManager.DataEntry<?>)var3.next();
                SyncType syncType = entry.getTrackedData().getSyncType();
                if (syncToAll) {
                    if (syncType == SyncType.TO_CLIENTS) {
                        break;
                    }
                } else if (syncType != SyncType.NOPE) {
                    break;
                }
            }

            dirtyEntries.add(entry);
        }
    }

    public EndimatedState getEndimatedState() {
        return this.endimatedState;
    }

    public Position getPos() {
        return this.pos;
    }

    public boolean isActive() {
        return this.isAlive();
    }

    @Shadow
    public boolean isAlive() {
        return false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.BEFORE), method = "writeNbt")
    private void writeTrackedData(NbtCompound compound, CallbackInfoReturnable<NbtCompound> info) {
        if (!this.dataMap.isEmpty()) {
            NbtList nbtElements = new NbtList();
            this.dataMap.forEach((trackedData, dataEntry) -> {
                if (trackedData.shouldSave()) {
                    NbtCompound dataTag = dataEntry.writeValue();
                    dataTag.putString("Id", Objects.requireNonNull(TrackedDataManager.INSTANCE.getKey(trackedData)).toString());
                    nbtElements.add(dataTag);
                }

            });
            compound.put("BlueprintTrackedData", nbtElements);
        }

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.BEFORE), method = "readNbt")
    public void read(NbtCompound compound, CallbackInfo info) {
        if (compound.contains("BlueprintTrackedData", 9)) {
            NbtList nbtElements = compound.getList("BlueprintTrackedData", 10);
            nbtElements.forEach((nbt) -> {
                NbtCompound nbtCompound = (NbtCompound)nbt;
                Identifier id = new Identifier(nbtCompound.getString("Id"));
                TrackedData<?> trackedData = TrackedDataManager.INSTANCE.getTrackedData(id);
                if (trackedData != null && trackedData.shouldSave()) {
                    IDataManager.DataEntry<?> dataEntry = new IDataManager.DataEntry<>(trackedData);
                    dataEntry.readValue(nbtCompound, true);
                    this.dataMap.put(trackedData, dataEntry);
                } else if (trackedData == null) {
                    Blueprint.LOGGER.warn("Received NBT for unknown Tracked Data: {}", id);
                }

            });
        }
    }

    @Inject(at = @At(value = "HEAD", shift = At.Shift.BY, by = 1), method = "baseTick")
    private void baseTick(CallbackInfo info) {
        this.endimateTick();
    }

//    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z"))
//    private boolean onIsSteppingCarefully(Entity instance) {
//        EntityStepCallback.invoke(this.world, this.getLandingPos(), instance);
//        return instance.bypassesSteppingEffects();
//    }
}

