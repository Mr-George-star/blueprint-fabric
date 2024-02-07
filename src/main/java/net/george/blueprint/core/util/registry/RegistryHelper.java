package net.george.blueprint.core.util.registry;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A class that works as a parent holder to children {@link ISubRegistryHelper}s.
 * <p>
 * A map is stored inside this that maps a {@link Registry} to a {@link ISubRegistryHelper} to get the child {@link ISubRegistryHelper} for a specific {@link Registry}.
 * A value put for a key in this map is a {@link ISubRegistryHelper} with the same parameterized type as the key.
 * </p>
 * Use the {@link #putSubHelper(Registry, ISubRegistryHelper)} method to put a {@link ISubRegistryHelper} for a {@link Registry}.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public class RegistryHelper {
    private final Map<Registry<?>, ISubRegistryHelper<?>> subHelpers = Maps.newHashMap();
    protected final String modId;

    public RegistryHelper(String modId) {
        this.modId = modId;
        this.putDefaultSubHelpers();
    }

    /**
     * Creates a new {@link RegistryHelper} with a specified mod ID and then accepts a consumer onto it.
     *
     * @param modId    The mod ID for this helper.
     * @param consumer A consumer to accept after the helper has been initialized.
     * @return A new {@link RegistryHelper} with a specified mod ID that has had a consumer accepted onto it.
     */
    public static RegistryHelper create(String modId, Consumer<RegistryHelper> consumer) {
        RegistryHelper helper = new RegistryHelper(modId);
        consumer.accept(helper);
        return helper;
    }

    /**
     * @return The mod id belonging to this {@link RegistryHelper}.
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Creates a {@link Identifier} for a string prefixed with the mod id.
     *
     * @param name The string to prefix.
     * @return A {@link Identifier} for a string prefixed with the mod id
     */
    public Identifier prefix(String name) {
        return new Identifier(this.modId, name);
    }

    /**
     * Puts a {@link ISubRegistryHelper} for a {@link Registry}.
     *
     * @param registry  The {@link Registry} to map the key to.
     * @param subHelper The {@link ISubRegistryHelper} to be mapped.
     * @param <T>       The type of {@link Registry}
     */
    public <T> void putSubHelper(Registry<T> registry, ISubRegistryHelper<T> subHelper) {
        this.subHelpers.put(registry, subHelper);
    }

    /**
     * Puts the default {@link ISubRegistryHelper}s onto the map.
     */
    protected void putDefaultSubHelpers() {
        this.putSubHelper(Registry.ITEM, new ItemSubRegistryHelper(this));
        this.putSubHelper(Registry.BLOCK, new BlockSubRegistryHelper(this));
        this.putSubHelper(Registry.SOUND_EVENT, new SoundSubRegistryHelper(this));
        this.putSubHelper(Registry.BLOCK_ENTITY_TYPE, new BlockEntitySubRegistryHelper(this));
        this.putSubHelper(Registry.ENTITY_TYPE, new EntitySubRegistryHelper(this));
        this.putSubHelper(BuiltinRegistries.BIOME, new BiomeSubRegistryHelper(this));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T, S extends ISubRegistryHelper<T>> S getSubHelper(Registry<T> registry) {
        S subHelper = (S) this.subHelpers.get(registry);
        if (subHelper == null) {
            throw new NullPointerException("No Sub Helper is registered for " + registry);
        }
        return subHelper;
    }

    @NotNull
    public <T extends AbstractSubRegistryHelper<Item>> T getItemSubHelper() {
        return this.getSubHelper(Registry.ITEM);
    }

    @NotNull
    public <T extends AbstractSubRegistryHelper<Block>> T getBlockSubHelper() {
        return this.getSubHelper(Registry.BLOCK);
    }

    @NotNull
    public <T extends AbstractSubRegistryHelper<BlockEntityType<?>>> T getBlockEntitySubHelper() {
        return this.getSubHelper(Registry.BLOCK_ENTITY_TYPE);
    }

    @NotNull
    public <T extends AbstractSubRegistryHelper<EntityType<?>>> T getEntitySubHelper() {
        return this.getSubHelper(Registry.ENTITY_TYPE);
    }

    @NotNull
    public <T extends AbstractSubRegistryHelper<Biome>> T getBiomeSubHelper() {
        return this.getSubHelper(BuiltinRegistries.BIOME);
    }

    /**
     * Registers all the mapped {@link ISubRegistryHelper}s.
     */
    public void register() {
        this.subHelpers.values().forEach(ISubRegistryHelper::register);
    }
}
