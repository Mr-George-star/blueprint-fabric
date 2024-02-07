package net.george.blueprint.core.endimator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

/**
 * The global registry for all {@link PlayableEndimation} instances.
 * <p>This provides the necessary storage for reading/writing and syncing {@link PlayableEndimation} instances.</p>
 * <p>All {@link PlayableEndimation} instances should get registered here during mod loading.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public enum PlayableEndimationManager {
    INSTANCE;

    private final BiMap<Identifier, PlayableEndimation> registry = HashBiMap.create();
    private final ObjectList<PlayableEndimation> byID = new ObjectArrayList<>(256);
    private final Object2IntMap<PlayableEndimation> toID = new Object2IntOpenHashMap<>();
    private int nextID;

    PlayableEndimationManager() {
        this.toID.defaultReturnValue(-1);
        this.registerPlayableEndimation(PlayableEndimation.BLANK);
    }

    /**
     * Registers a given {@link PlayableEndimation}.
     *
     * @param playableEndimation A {@link PlayableEndimation} to register.
     * @return The given {@link PlayableEndimation}.
     */
    @CanIgnoreReturnValue
    public PlayableEndimation registerPlayableEndimation(PlayableEndimation playableEndimation) {
        return this.registerPlayableEndimation(playableEndimation.identifier(), playableEndimation);
    }

    /**
     * Registers a given {@link PlayableEndimation} for a given {@link Identifier} key.
     *
     * @param key                A {@link Identifier} to use as the key.
     * @param playableEndimation A {@link PlayableEndimation} to register.
     * @return The given {@link PlayableEndimation}.
     */
    public synchronized PlayableEndimation registerPlayableEndimation(Identifier key, PlayableEndimation playableEndimation) {
        BiMap<Identifier, PlayableEndimation> registry = this.registry;
        if (registry.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate key for Playable Endimation: " + key);
        } else {
            registry.put(key, playableEndimation);
        }
        int nextID = this.nextID;
        this.byID.size(Math.max(this.byID.size(), nextID + 1));
        this.byID.set(nextID, playableEndimation);
        this.toID.put(playableEndimation, nextID);
        this.nextID++;
        return playableEndimation;
    }

    /**
     * Gets a {@link PlayableEndimation} by its {@link Identifier} key.
     *
     * @param key A {@link Identifier} key to look up.
     * @return A {@link PlayableEndimation} by its {@link Identifier} key, or null if no {@link PlayableEndimation} exists for the key.
     */
    @Nullable
    public PlayableEndimation getEndimation(Identifier key) {
        return this.registry.get(key);
    }

    /**
     * Gets the {@link Identifier} key of a given {@link PlayableEndimation}.
     *
     * @param playableEndimation A {@link PlayableEndimation} to look up.
     * @return The {@link Identifier} key of a given {@link PlayableEndimation}, or null if no key exists for the {@link PlayableEndimation}.
     */
    @Nullable
    public Identifier getKey(PlayableEndimation playableEndimation) {
        return this.registry.inverse().get(playableEndimation);
    }

    /**
     * Gets a {@link PlayableEndimation} by its ID.
     *
     * @param id An ID to look up.
     * @return A {@link PlayableEndimation} by its ID, or null if no {@link PlayableEndimation} exists for the given ID.
     */
    @Nullable
    public PlayableEndimation getEndimation(int id) {
        return this.byID.get(id);
    }

    /**
     * Gets the ID of a given {@link PlayableEndimation}.
     *
     * @param endimation A {@link PlayableEndimation} to look up.
     * @return The ID of a given {@link PlayableEndimation}, or -1 if no ID for the given {@link PlayableEndimation} exists.
     */
    public int getID(PlayableEndimation endimation) {
        return this.toID.getInt(endimation);
    }
}
