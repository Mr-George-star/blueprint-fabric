package net.george.blueprint.core.endimator.util;

import net.george.blueprint.core.endimator.Endimatable;
import net.george.blueprint.core.endimator.Endimator;
import net.george.blueprint.core.endimator.Endimation;
import net.george.blueprint.core.endimator.PlayableEndimation;
import net.george.blueprint.core.events.EntityRendererEvents;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A caching class for {@link Endimator} instances that belong to specific models.
 * <p>This is especially useful for processing {@link Endimation} instances on models you don't have direct access to, such as vanilla mobs.</p>
 * <p>It is recommended to add instances of this class to your mod's event bus to clear {@link Endimator} instances belonging to unused models.</p>
 *
 * @param <M> The type of model to map {@link Endimator} instances to.
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class ModelEndimatorCache<M> {
    private final Map<M, Endimator> endimatorMap = new WeakHashMap<>();
    private final Function<M, Endimator> factory;

    private ModelEndimatorCache(Function<M, Endimator> factory) {
        this.factory = factory;
        this.registerEvents();
    }

    /**
     * Creates a new {@link ModelEndimatorCache} instance for a type of model.
     *
     * @param factory A factory function to use for initializing a new {@link Endimator} instance for a model.
     * @param <M>     The type of model to map {@link Endimator} instances to.
     * @return A new {@link ModelEndimatorCache} instance for a type of model.
     */
    public static <M> ModelEndimatorCache<M> forType(Function<M, Endimator> factory) {
        return new ModelEndimatorCache<>(factory);
    }

    /**
     * Gets or creates an {@link Endimator} instance mapped to a given model.
     *
     * @param model A model to get its {@link Endimator} instance.
     * @return The {@link Endimator} instance mapped to a given model.
     */
    public Endimator getEndimator(M model) {
        return this.endimatorMap.computeIfAbsent(model, this.factory);
    }

    /**
     * Resets the {@link Endimator} instance mapped to a given model.
     *
     * @param model A model to get its {@link Endimator} instance.
     * @param mode  A {@link Endimator.ResetMode} to use.
     */
    public void resetEndimator(M model, Endimator.ResetMode mode) {
        this.getEndimator(model).reset(mode);
    }

    /**
     * Processes the applying of the {@link Endimator} instance belonging to a given model, using a given {@link Endimatable} instance to get the state of animation.
     *
     * @param model        A model to get its {@link Endimator} instance.
     * @param mode         A {@link Endimator.ResetMode} to use.
     * @param endimatable  An {@link Endimatable} instance to get the state of animation to use.
     * @param partialTicks The partial ticks.
     */
    public void endimate(M model, Endimator.ResetMode mode, Endimatable endimatable, float partialTicks) {
        PlayableEndimation playing = endimatable.getPlayingEndimation();
        if (playing != PlayableEndimation.BLANK) {
            Endimation endimation = playing.asEndimation();
            if (endimation != null) {
                float time = (endimatable.getAnimationTick() + partialTicks) * 0.05F;
                float length = endimation.getLength();
                if (time > length) {
                    time = length;
                }
                this.endimateModel(model, endimation, time, mode);
            }
        }
    }

    /**
     * Processes the applying of an {@link Endimation} instance on the {@link Endimator} instance belonging to a given model.
     *
     * @param model      A model to get its {@link Endimator} instance.
     * @param endimation An {@link Endimation} instance to process.
     * @param mode       A {@link Endimator.ResetMode} to use.
     * @param time       The time at which the given {@link Endimation} is playing at.
     */
    public void endimateModel(M model, Endimation endimation, float time, Endimator.ResetMode mode) {
        this.getEndimator(model).apply(endimation, time, mode);
    }

    public void registerEvents() {
        EntityRendererEvents.ADD_LAYERS.register((renderers, playerRenderers) -> this.endimatorMap.clear());
    }
}

