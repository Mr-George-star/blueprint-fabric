package net.george.blueprint.common.world.storage.tracking;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This interface handles all the management of the {@link TrackedData}s on an object.
 * This can effectively be used on any object type, but the groundwork must be done yourself.
 * If you wish to have this be used on a type of object other than an entity take a look at how this interface is used for the entity tracking system.
 * <p>This is Mixin'd into {@link Entity}, so casting an {@link Entity} to this interface is safe.</p>
 *
 * @author Mr.George
 */
@SuppressWarnings("unused")
public interface IDataManager {
    /**
     * Sets a value for a {@link TrackedData}.
     *
     * @param trackedData The {@link TrackedData} to the value for.
     * @param value       The value to set.
     * @param <T>         The type of value.
     */
    <T> void setValue(TrackedData<T> trackedData, T value);

    /**
     * Gets a value for a {@link TrackedData}.
     *
     * @param trackedData The {@link TrackedData} to get the value for.
     * @param <T>         The type of value.
     */
    <T> T getValue(TrackedData<T> trackedData);

    /**
     * @return Is this {@link IDataManager} dirty.
     */
    boolean isDirty();

    /**
     * Cleans all the dirty entries.
     */
    void clean();

    /**
     * Gets the map that stores all the {@link TrackedData} and their corresponding {@link DataEntry}s.
     *
     * @return The map that stores all the {@link TrackedData} and their corresponding {@link DataEntry}s.
     */
    Map<TrackedData<?>, DataEntry<?>> getDataMap();

    /**
     * Sets the map that stores all the {@link TrackedData} and their corresponding {@link DataEntry}s.
     *
     * @param dataEntryMap A new data map.
     */
    void setDataMap(Map<TrackedData<?>, DataEntry<?>> dataEntryMap);

    /**
     * @return The dirty entries.
     */
    Set<DataEntry<?>> getDirtyEntries();

    /**
     * @param syncToAll Should this filter for only {@link SyncType#TO_CLIENTS}.
     * @return The entries for a {@link SyncType}.
     */
    Set<DataEntry<?>> getEntries(boolean syncToAll);

    /**
     * A value class for a {@link TrackedData} key.
     *
     * @author SmellyModder (Luke Tonon)
     */
    class DataEntry<T> {
        private final TrackedData<T> trackedData;
        private T value;
        private boolean dirty;

        public DataEntry(TrackedData<T> trackedData) {
            this.trackedData = trackedData;
            this.value = trackedData.getDefaultValue();
        }

        /**
         * Reads a new entry from a {@link PacketByteBuf} instance.
         *
         * @param buffer A {@link PacketByteBuf} to read a new entry from.
         * @return A new entry from a {@link PacketByteBuf} instance.
         */
        public static DataEntry<?> read(PacketByteBuf buffer) {
            int id = buffer.readVarInt();
            TrackedData<?> trackedData = TrackedDataManager.INSTANCE.getTrackedData(id);
            Objects.requireNonNull(trackedData, String.format("Tracked Data does not exist for id %o", id));
            DataEntry<?> entry = new DataEntry<>(trackedData);
            entry.readValue(buffer.readNbt(), true);
            return entry;
        }

        /**
         * Gets this entry's {@link #trackedData}.
         *
         * @return This entry's {@link #trackedData}.
         */
        public TrackedData<T> getTrackedData() {
            return this.trackedData;
        }

        /**
         * Gets this entry's {@link #value}.
         *
         * @return This entry's {@link #value}.
         */
        public T getValue() {
            return this.value;
        }

        /**
         * Sets the {@link #value} of this entry.
         *
         * @param value A new value.
         * @param dirty If this entry should now be marked dirty.
         */
        public void setValue(T value, boolean dirty) {
            this.value = value;
            this.dirty = dirty;
        }

        /**
         * Marks this entry dirty.
         */
        public void markDirty() {
            this.dirty = true;
        }

        /**
         * Checks if this entry is marked dirty.
         *
         * @return If this entry is marked dirty.
         */
        public boolean isDirty() {
            return this.dirty;
        }

        /**
         * Marks this entry as clean (not dirty).
         */
        public void clean() {
            this.dirty = false;
        }

        /**
         * Writes this entry to a {@link PacketByteBuf} instance.
         *
         * @param buffer A {@link PacketByteBuf} to write this entry to.
         */
        public void write(PacketByteBuf buffer) {
            buffer.writeVarInt(TrackedDataManager.INSTANCE.getId(this.trackedData));
            buffer.writeNbt(this.writeValue());
        }

        /**
         * Writes this entry's {@link #value} into a {@link NbtCompound}.
         *
         * @return This entry's {@link #value} as a {@link NbtCompound}.
         */
        public NbtCompound writeValue() {
            return this.getTrackedData().getProcessor().write(this.value);
        }

        /**
         * Reads a new {@link #value} for this entry from a {@link NbtCompound}.
         *
         * @param compound A {@link NbtCompound} to read from.
         * @param dirty    If this entry should now be marked dirty.
         */
        public void readValue(NbtCompound compound, boolean dirty) {
            this.value = this.getTrackedData().getProcessor().read(compound);
            this.dirty = dirty;
        }
    }
}
