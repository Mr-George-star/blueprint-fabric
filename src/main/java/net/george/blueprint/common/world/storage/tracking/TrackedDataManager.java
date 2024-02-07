package net.george.blueprint.common.world.storage.tracking;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.george.blueprint.core.events.EntityEvents;
import net.george.blueprint.core.events.PlayerEvents;
import net.george.blueprint.core.util.NetworkUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * This class is basically an external version of the {@link DataTracker} System.
 * <p>
 * The {@link DataTracker} System is used in Minecraft to sync data on entities from the server to clients.
 * This system is very similar to the {@link DataTracker} System with a few differences.
 * One difference being all reading and writing is done with NBT so it can be used for both networking and storage.
 * These differences and advantages may make these more favorable than capabilities depending on the use case.
 * </p>
 * <p> Another important detail is this system can be applied to any other object, but you must do the groundwork yourself. </p>
 * <p> To register a {@link TrackedData} use {@link #registerData(Identifier, TrackedData)} during the common setup of your mod. </p>
 *
 * @author Mr.George
 */
@SuppressWarnings("unused")
public enum TrackedDataManager {
    INSTANCE;

    private final BiMap<Identifier, TrackedData<?>> dataMap = HashBiMap.create();
    private final BiMap<Integer, TrackedData<?>> idMap = HashBiMap.create();
    private int nextId = 0;

    TrackedDataManager() {
        onStartTracking();
        onChangeDimension();
        onPlayerClone();
        onEntityJoinWorld();
    }

    /**
     * Registers a {@link TrackedData} for a {@link Identifier} key.
     * Call this in the common setup of your mod.
     *
     * @param key         The key to register the {@link TrackedData} for.
     * @param trackedData The {@link TrackedData} to register.
     */
    public synchronized void registerData(Identifier key, TrackedData<?> trackedData) {
        if (this.dataMap.containsKey(key)) {
            throw new IllegalArgumentException(String.format("A Tracked Data with key '%s' is already registered!", key));
        }
        this.dataMap.put(key, trackedData);
        this.idMap.put(this.nextId, trackedData);
        this.nextId++;
    }

    /**
     * Sets a value for a {@link TrackedData} for an {@link Entity}.
     *
     * @param entity      The entity to set the value for.
     * @param trackedData The {@link TrackedData} to set the value for.
     * @param value       The value to set for the {@link TrackedData}.
     * @param <T>         The type of value.
     */
    public <T> void setValue(Entity entity, TrackedData<T> trackedData, T value) {
        if (!this.dataMap.containsValue(trackedData)) {
            throw new IllegalArgumentException(String.format("No key is registered for this Tracked Data: %s", trackedData));
        }
        ((IDataManager)entity).setValue(trackedData, value);
    }

    /**
     * Gets a value for a {@link TrackedData} for an {@link Entity}.
     *
     * @param entity      The entity to set the value from.
     * @param trackedData The {@link TrackedData} to get the value for.
     * @param <T>         The type of value to get.
     * @return The value gotten from the {@link TrackedData} from the {@link Entity}.
     */
    public <T> T getValue(Entity entity, TrackedData<T> trackedData) {
        if (!this.dataMap.containsValue(trackedData)) {
            throw new IllegalArgumentException(String.format("No key is registered for this Tracked Data: %s", trackedData));
        }
        return ((IDataManager)entity).getValue(trackedData);
    }

    /**
     * Gets a {@link TrackedData} by a {@link Identifier}.
     *
     * @param identifier The {@link Identifier} to lookup.
     * @return The {@link TrackedData} registered for the supplied {@link Identifier}.
     */
    @Nullable
    public TrackedData<?> getTrackedData(Identifier identifier) {
        return this.dataMap.get(identifier);
    }

    /**
     * Gets the {@link Identifier} key for a {@link TrackedData}.
     *
     * @param trackedData The {@link TrackedData} to lookup.
     * @return The {@link Identifier} key for the registered {@link TrackedData}.
     */
    @Nullable
    public Identifier getKey(TrackedData<?> trackedData) {
        return this.dataMap.inverse().get(trackedData);
    }

    /**
     * Gets a {@link TrackedData} by its registry id.
     *
     * @param id The id to lookup.
     * @return The {@link TrackedData} for the supplied id.
     */
    @Nullable
    public TrackedData<?> getTrackedData(int id) {
        return this.idMap.get(id);
    }

    /**
     * Gets the id of a {@link TrackedData}.
     *
     * @param trackedData The {@link TrackedData} to get the id for.
     * @return The id of the supplied id.
     */
    public int getId(TrackedData<?> trackedData) {
        return this.idMap.inverse().get(trackedData);
    }

    public static void onStartTracking() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (!trackedEntity.world.isClient) {
                Set<IDataManager.DataEntry<?>> entries = ((IDataManager) trackedEntity).getEntries(true);
                if (!entries.isEmpty()) {
                    NetworkUtil.updateTrackedData(player, trackedEntity.getId(), entries);
                }
            }
        });
    }

    public static void onEntityJoinWorld() {
        EntityEvents.JOIN_WORLD.register((entity, world, existing) -> {
            if (!entity.world.isClient) {
                Set<IDataManager.DataEntry<?>> entries = ((IDataManager)entity).getEntries(false);
                if (!entries.isEmpty()) {
                    NetworkUtil.updateTrackedData(entity, entries);
                }
            }
        });
    }

    public static void onPlayerClone() {
        PlayerEvents.CLONE.register((newPlayer, oldPlayer, alive) -> {
            if (!oldPlayer.world.isClient) {
                Map<TrackedData<?>, IDataManager.DataEntry<?>> dataMap = ((IDataManager)oldPlayer).getDataMap();
                if (alive) {
                    dataMap.entrySet().removeIf(entry -> !entry.getKey().isPersistent());
                }
                dataMap.values().forEach(IDataManager.DataEntry::markDirty);
                ((IDataManager)newPlayer).setDataMap(dataMap);
            }
        });
    }

    public static void onChangeDimension() {
        PlayerEvents.CHANGE_DIMENSION.register((player, fromDimension, toDimension) -> {
            IDataManager dataManager = (IDataManager)player;
            Map<TrackedData<?>, IDataManager.DataEntry<?>> dataMap = dataManager.getDataMap();
            dataMap.values().forEach(IDataManager.DataEntry::markDirty);
            ((IDataManager)player).setDataMap(dataMap);
        });
    }
}
