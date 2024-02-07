package net.george.blueprint.common.world.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

/**
 * Implemented on types that will store global NBT data for a Minecraft Save.
 * This interface should only be used on the server side.
 * This main purpose of this interface is for storing global data for a Minecraft Save.
 *
 * @author SmellyModder (Luke Tonon)
 * @see PersistentState
 */
public interface GlobalStorage {
    Map<Identifier, GlobalStorage> STORAGES = new HashMap<>();

    /**
     * Adds a {@link GlobalStorage} to the {@link #STORAGES} map and returns the {@link GlobalStorage}.
     * Use this to have your {@link GlobalStorage} be saved and loaded.
     *
     * @param key     The ID of the storage.
     * @param storage The {@link GlobalStorage} to add to the {@link #STORAGES} map.
     * @param <S>     The type of {@link GlobalStorage}.
     * @return The supplied {@link GlobalStorage}.
     * @throws IllegalStateException if the storage is created after {@link GlobalStorageManager} has loaded its NBT.
     */
    static <S extends GlobalStorage> S createStorage(Identifier key, S storage) {
        if (GlobalStorageManager.isLoaded()) {
            throw new IllegalStateException(String.format("Global Storage with id %s was created after Global Storage Manager loaded!", key));
        }
        STORAGES.put(key, storage);
        return storage;
    }

    /**
     * Called when saving this {@link GlobalStorage} to NBT.
     *
     * @return The serialized NBT data of this {@link GlobalStorage}.
     */
    NbtCompound toNbt();

    /**
     * Called when loading the saved NBT data for this {@link GlobalStorage}.
     *
     * @param nbt The deserialized NBT data of this {@link GlobalStorage}.
     */
    void fromNbt(NbtCompound nbt);
}
