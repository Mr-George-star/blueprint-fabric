package net.george.blueprint.core.util.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * A simplified version of the {@link Registry} class.
 * <p>This class is not an instance of {@link Registry}</p>
 *
 * @param <T> The type of object for the registry.
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class BasicRegistry<T> implements Codec<T> {
    private final Lifecycle lifecycle;
    private final BiMap<Identifier, T> map = HashBiMap.create();

    public BasicRegistry(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public BasicRegistry() {
        this.lifecycle = Lifecycle.stable();
    }

    /**
     * Registers a value for a given {@link Identifier}.
     *
     * @param name  A {@link Identifier} to register the value with.
     * @param value A value to register.
     */
    public void register(Identifier name, T value) {
        this.map.put(name, value);
    }

    /**
     * Gets this registry's {@link #lifecycle}.
     *
     * @return This registry's {@link #lifecycle}.
     */
    @Nonnull
    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    /**
     * Gets a value for a given {@link Identifier} name.
     *
     * @param name A {@link Identifier} name to look up the value with.
     * @return A value for a given {@link Identifier} name, or null if there's no value for the given {@link Identifier}.
     */
    @Nullable
    public T getValue(Identifier name) {
        return this.map.get(name);
    }

    /**
     * Gets a {@link Identifier} for a given value.
     *
     * @param value A value to get a {@link Identifier} for.
     * @return A {@link Identifier} for a given value.
     */
    @Nullable
    public Identifier getKey(T value) {
        return this.map.inverse().get(value);
    }

    /**
     * Gets all the {@link Identifier} keys in this registry.
     *
     * @return A set of all the {@link Identifier} keys in this registry.
     */
    @Nonnull
    public Set<Identifier> keySet() {
        return this.map.keySet();
    }

    /**
     * Gets all the registered values in this registry.
     *
     * @return A set of all the registered values in this registry.
     */
    @Nonnull
    public Set<T> getValues() {
        return this.map.values();
    }

    /**
     * Gets all the entries in this registry.
     *
     * @return A set of all the entries in this registry.
     */
    @Nonnull
    public Set<Map.Entry<Identifier, T>> getEntries() {
        return this.map.entrySet();
    }

    /**
     * Checks if this registry has an entry with a given {@link Identifier} name.
     *
     * @param name A {@link Identifier} name to check.
     * @return If this registry has an entry with a given {@link Identifier} name.
     */
    public boolean containsKey(Identifier name) {
        return this.map.containsKey(name);
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
        return Identifier.CODEC.decode(ops, input).flatMap((encodedRegistryPair) -> {
            Identifier name = encodedRegistryPair.getFirst();
            T value = this.getValue(name);
            return value == null ? DataResult.error("Unknown registry key: " + name) : DataResult.success(Pair.of(value, encodedRegistryPair.getSecond()), this.lifecycle);
        });
    }

    @Override
    public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix) {
        Identifier name = this.getKey(input);
        if (name == null) {
            return DataResult.error("Unknown registry element: " + prefix);
        }
        return ops.mergeToPrimitive(prefix, ops.createString(name.toString())).setLifecycle(this.lifecycle);
    }
}
