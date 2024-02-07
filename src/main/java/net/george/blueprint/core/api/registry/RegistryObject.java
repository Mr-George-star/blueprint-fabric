package net.george.blueprint.core.api.registry;

import com.google.common.base.Preconditions;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.registry.RegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class RegistryObject<T> implements Supplier<T> {
    @Nullable
    private T value;
    @NotNull
    private final Identifier name;
    @NotNull
    private final Registry<T> registry;
    @NotNull
    private final RegistryKey<T> key;
    @Nullable
    private RegistryEntry<T> entry;
    private boolean registered = false;

    private RegistryObject(@NotNull Identifier name, @NotNull Class<T> registryType) {
        this.name = name;
        this.registry = RegistryManager.getRegistry(registryType);
        this.key = RegistryKey.of(this.registry.getKey(), name);
    }

    private RegistryObject(@NotNull Identifier name, @NotNull Identifier registryName) {
        RegistryKey<Registry<T>> registryKey = RegistryKey.ofRegistry(registryName);

        this.name = name;
        this.registry = Preconditions.checkNotNull(getRegistry(registryKey), "There is no registry named " + registryName + " in game.");
        this.key = RegistryKey.of(registryKey, name);
    }

    private RegistryObject(@NotNull Identifier name, @NotNull Registry<T> registry) {
        this.name = name;
        this.registry = registry;
        this.key =  RegistryKey.of(this.registry.getKey(), name);
    }

    private RegistryObject(@NotNull Identifier name, @NotNull Registry<T> registry, @NotNull RegistryKey<T> registryKey, @NotNull RegistryEntry<T> registryEntry) {
        this.name = name;
        this.registry = registry;
        this.key = registryKey;
    }

    /**
     * @deprecated The uniqueness of registry super types will not be guaranteed starting in 1.19.
     * Use {@link #of(Identifier, Identifier)}.
     */
    @Deprecated(since = "1.18.2")
    @NotNull
    public static <T> RegistryObject<T> of(@NotNull Identifier name, @NotNull Class<T> registryType) {
        return new RegistryObject<>(name, registryType);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry name.
     * <p>
     * If a registry with the given name cannot be found, an exception will be thrown when trying to fill this {@link RegistryObject}.
     *
     * @param name the name of the object to look up in a registry
     * @param registryName the name of the registry. Supports lookups on {@link BuiltinRegistries} and {@link Registry}.
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #of(Identifier, Identifier)
     * @see #of(Identifier, Registry, RegistryKey, RegistryEntry)
     */
    @NotNull
    public static <T> RegistryObject<T> of(@NotNull Identifier name, @NotNull Identifier registryName) {
        return new RegistryObject<>(name, registryName);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from the provided registry once it is ready.
     *
     * @param name the name of the object to look up in the registry
     * @param registry the registry
     * @return a {@link RegistryObject} that stores the value of an object from the provided registry once it is ready
     */
    @NotNull
    public static <T> RegistryObject<T> of(@NotNull Identifier name, @NotNull Registry<T> registry) {
        return new RegistryObject<>(name, registry);
    }

    @NotNull
    public static <T> RegistryObject<T> of(@NotNull Identifier name, @NotNull Registry<T> registry, @NotNull RegistryKey<T> registryKey, @NotNull RegistryEntry<T> registryEntry) {
        return new RegistryObject<>(name, registry, registryKey, registryEntry);
    }

    /**
     * Retrieves the wrapped object in the registry.
     * This value will automatically be updated when the backing registry is updated.
     *
     * @throws NullPointerException If the value is null. Use {@link #isPresent()} to check if the value exists first.
     * @see #isPresent()
     * @see #orElse(Object)
     * @see #orElseGet(Supplier)
     * @see #orElseThrow(Supplier)
     */
    @NotNull
    @Override
    public T get() {
        Preconditions.checkNotNull(this.value, "Registry Object not present: " + this.name + ".");
        return this.value;
    }

    void updateReference(T value) {
        this.value = value;
        if (!this.registered) {
            Registry.register(this.registry, this.name, this.value);
            this.registered = true;
        }
        this.entry = this.registry.getOrCreateEntry(this.key);
        Blueprint.LOGGER.debug(Blueprint.CORE, "Registry Object " + this.value + ", registry type " + this.registry + " just registered!");
    }

    private static boolean registryExists(Identifier registryName) {
        return Registry.REGISTRIES.containsId(registryName) || BuiltinRegistries.REGISTRIES.containsId(registryName);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Registry<T> getRegistry(RegistryKey<Registry<T>> key) {
        Registry<T> registry = Registry.REGISTRIES.get((RegistryKey)key);
        Registry<T> builtinRegistry = BuiltinRegistries.REGISTRIES.get((RegistryKey)key);
        return registry == null ? builtinRegistry : registry;
    }

    /**
     * @return The {@link Identifier} of this {@link RegistryObject}.
     */
    @NotNull
    public Identifier getId() {
        return this.name;
    }

    /**
     * @return The registry type of this {@link RegistryObject}.
     */
    @NotNull
    public Registry<T> getRegistry() {
        return this.registry;
    }

    /**
     * Returns the resource key that points to the registry and name of this registry object.
     *
     * @return the resource key that points to the registry and name of this registry object
     */
    @NotNull
    public RegistryKey<T> getKey() {
        return this.key;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public Optional<RegistryEntry<T>> getEntry() {
        if (this.entry == null && registryExists(this.key.method_41185())) {
            Identifier registryName = this.key.method_41185();
            Registry<T> registry = (Registry<T>) Registry.REGISTRIES.get(registryName);
            if (registry == null) {
                registry = (Registry<T>) BuiltinRegistries.REGISTRIES.get(registryName);
            }

            if (registry != null) {
                this.entry = registry.getOrCreateEntry(this.key);
            }
        }

        return Optional.ofNullable(this.entry);
    }

    @NotNull
    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    @NotNull
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @NotNull
    public Spliterator<T> spliterator() {
        return stream().spliterator();
    }

    @NotNull
    public Collection<T> collection() {
        return stream().toList();
    }

    /**
     * Return {@code true} if there is a mod object present, otherwise {@code false}.
     *
     * @return {@code true} if there is a mod object present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.value != null;
    }

    /**
     * If a mod object is present, invoke the specified consumer with the object,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a mod object is present
     * @throws NullPointerException if mod object is present and {@code consumer} is
     * null
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(get());
        }
    }

    /**
     * If a mod object is present, and the mod object matches the given predicate,
     * return an {@code RegistryObject} describing the value, otherwise return an
     * empty {@code RegistryObject}.
     *
     * @param predicate a predicate to apply to the mod object, if present
     * @return an {@code RegistryObject} describing the value of this {@code RegistryObject}
     * if a mod object is present and the mod object matches the given predicate,
     * otherwise an empty {@code RegistryObject}
     * @throws NullPointerException if the predicate is null
     */
    @Nullable
    public RegistryObject<T> filter(Predicate<? super T> predicate) {
        Preconditions.checkNotNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(get()) ? this : null;
        }
    }

    /**
     * If a mod object is present, apply the provided mapping function to it,
     * and if the result is non-null, return an {@code Optional} describing the
     * result.  Otherwise, return an empty {@code Optional}.
     *
     * @apiNote This method supports post-processing on optional values, without
     * the need to explicitly check for a return status.
     *
     * @param <V> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the mod object, if present
     * @return an {@code Optional} describing the result of applying a mapping
     * function to the mod object of this {@code RegistryObject}, if a mod object is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null
     */
    public <V> Optional<V> map(Function<? super T, ? extends V> mapper) {
        Preconditions.checkNotNull(mapper);
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(get()));
        }
    }

    /**
     * If a value is present, apply the provided {@code Optional}-bearing
     * mapping function to it, return that result, otherwise return an empty
     * {@code Optional}.  This method is similar to {@link #map(Function)},
     * but the provided mapper is one whose result is already an {@code Optional},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code Optional}.
     *
     * @param <V> The type parameter to the {@code Optional} returned by
     * @param mapper a mapping function to apply to the mod object, if present
     *           the mapping function
     * @return the result of applying an {@code Optional}-bearing mapping
     * function to the value of this {@code Optional}, if a value is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null or returns
     * a null result
     */
    public <V> Optional<V> flatMap(Function<? super T, Optional<V>> mapper) {
        Preconditions.checkNotNull(mapper);
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Preconditions.checkNotNull(mapper.apply(get()));
        }
    }

    /**
     * If a mod object is present, lazily apply the provided mapping function to it,
     * returning a supplier for the transformed result. If this object is empty, or the
     * mapping function returns {@code null}, the supplier will return {@code null}.
     *
     * @apiNote This method supports post-processing on optional values, without
     * the need to explicitly check for a return status.
     *
     * @param <V> The type of the result of the mapping function
     * @param mapper A mapping function to apply to the mod object, if present
     * @return A {@code Supplier} lazily providing the result of applying a mapping
     * function to the mod object of this {@code RegistryObject}, if a mod object is present,
     * otherwise a supplier returning {@code null}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <V> Supplier<V> lazyMap(Function<? super T, ? extends V> mapper) {
        Preconditions.checkNotNull(mapper);
        return () -> isPresent() ? mapper.apply(get()) : null;
    }

    /**
     * Return the mod object if present, otherwise return {@code other}.
     *
     * @param other the mod object to be returned if there is no mod object present, may
     * be null
     * @return the mod object, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    /**
     * Return the mod object if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no mod object
     * is present
     * @return the mod object if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if mod object is not present and {@code other} is
     * null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    /**
     * Return the contained mod object, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @apiNote A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <V> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present mod object
     * @throws V if there is no mod object present
     * @throws NullPointerException if no mod object is present and
     * {@code exceptionSupplier} is null
     */
    public <V extends Throwable> T orElseThrow(Supplier<? extends V> exceptionSupplier) throws V {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistryObject<?> that)) {
            return false;
        }
        return com.google.common.base.Objects.equal(this.value, that.value) && com.google.common.base.Objects.equal(this.name, that.name) && com.google.common.base.Objects.equal(this.registry, that.registry);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.value, this.name, this.registry);
    }

    @Override
    public String toString() {
        return "RegistryObject[" +
                "value=" + this.value +
                ", name=" + this.name +
                ", registry=" + this.registry +
                ']';
    }
}
