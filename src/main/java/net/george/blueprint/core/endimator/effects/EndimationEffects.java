package net.george.blueprint.core.endimator.effects;

import net.george.blueprint.core.endimator.effects.particle.ParticleEndimationEffect;
import net.george.blueprint.core.endimator.effects.shaking.ShakeEndimationEffect;
import net.george.blueprint.core.endimator.effects.sound.SoundEndimationEffect;
import net.george.blueprint.core.util.registry.BasicRegistry;
import net.minecraft.util.Identifier;

/**
 * The registry class for all {@link EndimationEffect} instances.
 * <p>This registry is responsible for handling the lookup of effect types when serializing and deserializing {@link ConfiguredEndimationEffect} instances.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class EndimationEffects {
    public static final BasicRegistry<EndimationEffect<?>> REGISTRY = new BasicRegistry<>();

    public static final ParticleEndimationEffect PARTICLE = register("particle", new ParticleEndimationEffect());
    public static final SoundEndimationEffect SOUND = register("sound", new SoundEndimationEffect());
    public static final ShakeEndimationEffect SHAKE = register("shake", new ShakeEndimationEffect());

    private static <E extends EndimationEffect<?>> E register(String name, E effect) {
        REGISTRY.register(new Identifier(name), effect);
        return effect;
    }

    /**
     * Registers an {@link EndimationEffect} instance with a given {@link Identifier} name.
     *
     * @param name   A {@link Identifier} name for the effect.
     * @param effect An {@link EndimationEffect} instance to register.
     * @param <E>    The type of {@link EndimationEffect} to register.
     * @return The given {@link EndimationEffect} instance.
     */
    public static synchronized <E extends EndimationEffect<?>> E register(Identifier name, E effect) {
        REGISTRY.register(name, effect);
        return effect;
    }
}
