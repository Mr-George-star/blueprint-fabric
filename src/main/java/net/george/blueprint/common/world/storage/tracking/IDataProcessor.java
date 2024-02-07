package net.george.blueprint.common.world.storage.tracking;

import net.minecraft.nbt.NbtCompound;

/**
 * A simple interface that reads and writes NBT for a type of value.
 *
 * @param <T> The type to write and read.
 * @author Mr.George
 */
@SuppressWarnings("unused")
public interface IDataProcessor<T> {
    /**
     * Writes a type to a {@link NbtCompound}.
     *
     * @param type An object of the type to write.
     * @return The object serialized to {@link NbtCompound}.
     */
    NbtCompound write(T type);

    /**
     * Reads a type from a {@link NbtCompound}.
     *
     * @param compound The {@link NbtCompound} to read.
     * @return The type deserialized from a {@link NbtCompound}.
     */
    T read(NbtCompound compound);
}
