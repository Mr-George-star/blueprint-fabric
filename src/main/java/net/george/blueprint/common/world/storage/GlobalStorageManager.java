package net.george.blueprint.common.world.storage;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.george.blueprint.core.Blueprint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

/**
 * Handles the reading and writing of registered {@link GlobalStorage}s.
 *
 * @author SmellyModder (Luke Tonon)
 * @see PersistentState
 * @see GlobalStorage
 */
public final class GlobalStorageManager extends PersistentState {
    private static final String KEY = Blueprint.MOD_ID + "_storage";
    private static boolean loaded = false;

    private GlobalStorageManager() {
        super();
    }

    @CanIgnoreReturnValue
    public static GlobalStorageManager getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(compound -> {
            loaded = true;
            NbtList storageNbtList = compound.getList("storages", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < storageNbtList.size(); i++) {
                NbtCompound storageNbt = storageNbtList.getCompound(i);
                GlobalStorage storage = GlobalStorage.STORAGES.get(new Identifier(storageNbt.getString("id")));
                if (storage != null) {
                    storage.fromNbt(storageNbt);
                }
            }
            return new GlobalStorageManager();
        }, GlobalStorageManager::new, KEY);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        NbtList storageList = new NbtList();
        GlobalStorage.STORAGES.forEach((key, value) -> {
            NbtCompound storageNbt = value.toNbt();
            storageNbt.putString("id", key.toString());
            storageList.add(storageNbt);
        });
        compound.put("storages", storageList);
        return compound;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
