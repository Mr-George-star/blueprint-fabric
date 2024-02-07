package net.george.blueprint.core.api.registry;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.registry.RegistryManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Utility class to help with managing registry entries.
 * Suppliers should return NEW instances every time.
 * <p>
 * Example Usage:
 * <pre>{@code
 *   private static final DeferredRegister<Item> ITEMS = DeferredRegister.of(Registry.ITEM, MOD_ID);
 *   private static final DeferredRegister<Block> BLOCKS = DeferredRegister.of(Registry.BLOCK, MOD_ID);
 *
 *   public static final RegistryObject<Block> ROCK_BLOCK = BLOCKS.register("rock", () -> new Block(FabricBlockSettings.of(Material.STONE)));
 *   public static final RegistryObject<Item> ROCK_ITEM = ITEMS.register("rock", () -> new BlockItem(ROCK_BLOCK.get(), new FabricItemSettings().group(ItemGroup.MISC)));
 *
 *   @Override
 *   public void onInitialize() {
 *       ITEMS.register();
 *       BLOCKS.register();
 *   }
 *}</pre>
 *
 * @param <T> The base registry type
 */
@SuppressWarnings("unused")
public class DeferredRegister<T> {
    @NotNull
    private final Registry<T> registry;
    @NotNull
    private final String modid;
    private final Map<RegistryObject<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());
    @Nullable
    private SetMultimap<TagKey<T>, Supplier<T>> optionalTags;

    private DeferredRegister(@NotNull Registry<T> registry, @NotNull String modid) {
        this.registry = registry;
        this.modid = modid;
    }

    /**
     * {@link DeferredRegister} factory for registries that exist <i>before</i> this {@link DeferredRegister} is created.
     * <p>
     * If you have a supplier, <u>do not use this method.</u>
     * Instead, use one of the other factories that takes in a registry key or registry name.
     *
     * @param registry the forge registry to wrap
     * @param modid the namespace for all objects registered to this {@link DeferredRegister}
     * @see #of(RegistryKey, String)
     * @see #of(Identifier, String)
     */
    @NotNull
    public static <T> DeferredRegister<T> of(@NotNull Registry<T> registry, @NotNull String modid) {
        return new DeferredRegister<>(registry, modid);
    }

    /**
     * @deprecated Use {@link #of(Identifier, String)} and {@link #of(RegistryKey, String)} instead
     */
    @Deprecated(since = "1.18.2")
    @NotNull
    public static <T> DeferredRegister<T> of(@NotNull Class<T> registryType, @NotNull String modid) {
        return new DeferredRegister<>(RegistryManager.getRegistry(registryType), modid);
    }

    /**
     * {@link DeferredRegister} factory for custom {@link Registry registries},
     * or {@link BuiltinRegistries built-in registries} to lookup based on the provided registry key.
     * Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link RegistryObject}s made from this {@link DeferredRegister} will throw an exception.
     *
     * @param key the key of the registry to reference
     * @param modid the namespace for all objects registered to this {@link DeferredRegister}
     * @see #of(Registry, String)
     * @see #of(Identifier, String)
     */
    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> DeferredRegister<T> of(@NotNull RegistryKey<? extends Registry<T>> key, @NotNull String modid) {
        return new DeferredRegister<T>(Preconditions.checkNotNull(Registry.REGISTRIES.get((RegistryKey)key)), modid);
    }

    /**
     * {@link DeferredRegister} factory for custom {@link Registry registries},
     * or {@link BuiltinRegistries built-in registries} to lookup based on the provided registry name.
     * Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link RegistryObject}s made from this DeferredRegister will throw an exception.
     *
     * @param registryName The name of the registry, should include namespace. May come from another DeferredRegister through {@link #getRegistryName()}.
     * @param modid The namespace for all objects registered to this DeferredRegister
     * @see #of(Registry, String)
     * @see #of(RegistryKey, String)
     */
    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> DeferredRegister<T> of(@NotNull Identifier registryName, @NotNull String modid) {
        return new DeferredRegister<T>(Preconditions.checkNotNull(Registry.REGISTRIES.get((RegistryKey)RegistryKey.ofRegistry(registryName))), modid);
    }

    /**
     * Adds a new supplier to the list of entries to be registered, and returns a {@link RegistryObject} that will be populated with the created entry automatically.
     *
     * @param name The new entry's name, it will automatically have the modid prefixed.
     * @param entrySupplier A factory for the new entry, it should return a new instance every time it is called.
     * @return A {@link RegistryObject} that will be updated with when the entries in the registry change.
     */
    @NotNull
    @SuppressWarnings({"unchecked"})
    public <V extends T> RegistryObject<V> register(@NotNull String name, @NotNull Supplier<? extends V> entrySupplier) {
        Identifier id = new Identifier(this.modid, name);
        RegistryObject<V> result = (RegistryObject<V>)RegistryObject.of(id, this.registry);
        result.updateReference(entrySupplier.get());

        if (this.entries.containsKey(result)) {
            throw new IllegalStateException("Duplicate registration " + name + "!");
        } else {
            this.entries.put((RegistryObject<T>)result, entrySupplier);
        }
        return result;
    }

    /**
     * Creates a tag key based on the current modid and provided path as the location and the registry name linked to this {@link DeferredRegister}.
     * To control the namespace, use {@link #createTagKey(Identifier)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #of(Identifier, String) a registry name} or {@link #of(Registry, String) registry}.
     * @see #createTagKey(Identifier)
     * @see #createOptionalTagKey(String, Set)
     */
    @NotNull
    public TagKey<T> createTagKey(@NotNull String path) {
        Preconditions.checkNotNull(path);
        return createTagKey(new Identifier(this.modid, path));
    }

    /**
     * Creates a tag key based on the provided resource location and the registry name linked to this {@link DeferredRegister}.
     * To use the current modid as the namespace, use {@link #createTagKey(String)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #of(Identifier, String) a registry name} or {@link #of(Registry, String) registry}.
     * @see #createTagKey(String)
     * @see #createOptionalTagKey(Identifier, Set)
     */
    @NotNull
    public TagKey<T> createTagKey(@NotNull Identifier id) {
        Preconditions.checkNotNull(id);
        return TagKey.of(this.registry.getKey(), id);
    }

    /**
     * Creates a tag key with the current modid and provided path that will use the set of defaults if the tag is not loaded from any datapacks.
     * Useful on the client side when a server may not provide a specific tag.
     * To control the namespace, use {@link #createOptionalTagKey(Identifier, Set)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #of(Identifier, String) a registry name} or {@link #of(Registry, String) registry}.
     * @see #createTagKey(String)
     * @see #createTagKey(Identifier)
     * @see #createOptionalTagKey(Identifier, Set)
     * @see #addOptionalTagDefaults(TagKey, Set)
     */
    @NotNull
    public TagKey<T> createOptionalTagKey(@NotNull String path, @NotNull Set<? extends Supplier<T>> defaults) {
        Preconditions.checkNotNull(path);
        return createOptionalTagKey(new Identifier(this.modid, path), defaults);
    }

    /**
     * Creates a tag key with the provided location that will use the set of defaults if the tag is not loaded from any datapacks.
     * Useful on the client side when a server may not provide a specific tag.
     * To use the current modid as the namespace, use {@link #createOptionalTagKey(String, Set)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #of(Identifier, String) a registry name} or {@link #of(Registry, String) registry}.
     * @see #createTagKey(String)
     * @see #createTagKey(Identifier)
     * @see #createOptionalTagKey(String, Set)
     * @see #addOptionalTagDefaults(TagKey, Set)
     */
    @NotNull
    public TagKey<T> createOptionalTagKey(@NotNull Identifier id, @NotNull Set<? extends Supplier<T>> defaults) {
        TagKey<T> tagKey = createTagKey(id);
        addOptionalTagDefaults(tagKey, defaults);
        return tagKey;
    }

    /**
     * Adds defaults to an existing tag key.
     * The set of defaults will be bound to the tag if the tag is not loaded from any datapacks.
     * Useful on the client side when a server may not provide a specific tag.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #of(Identifier, String) a registry name} or {@link #of(Registry, String) registry}.
     * @see #createOptionalTagKey(String, Set)
     * @see #createOptionalTagKey(Identifier, Set)
     */
    public void addOptionalTagDefaults(@NotNull TagKey<T> name, @NotNull Set<? extends Supplier<T>> defaults) {
        Preconditions.checkNotNull(defaults);
        if (this.optionalTags == null) {
            this.optionalTags = Multimaps.newSetMultimap(new IdentityHashMap<>(), HashSet::new);
        }
        this.optionalTags.putAll(name, defaults);
    }

    public void register() {
        Blueprint.LOGGER.debug(Blueprint.CORE, "Deferred Register " + this + " just registering.");
    }

    /**
     * @return The unmodifiable view of registered entries. Useful for bulk operations on all values.
     */
    @NotNull
    public Collection<RegistryObject<T>> getEntrySet() {
        return this.entriesView;
    }

    /**
     * @return The map of registered entries. Useful for do some operation for registered entries.
     */
    @NotNull
    public Map<RegistryObject<T>, Supplier<? extends T>> getEntries() {
        return this.entries;
    }

    /**
     * @return The registry name stored in this deferred register. Useful for creating new deferred registers based on an existing one.
     */
    @NotNull
    public Identifier getRegistryName() {
        return this.registry.getKey().getValue();
    }

    @Override
    public String toString() {
        return "DeferredRegister[" +
                "registry=" + this.registry +
                ", modid='" + this.modid +
                ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DeferredRegister<?> that)) {
            return false;
        }
        return com.google.common.base.Objects.equal(this.registry, that.registry) && com.google.common.base.Objects.equal(this.modid, that.modid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.registry, this.modid);
    }
}
