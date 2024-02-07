package net.george.blueprint.core.endimator.effects;

import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

/**
 * The interface for defining an object that can process {@link ConfiguredEndimationEffect} instances.
 * <p>This gets mixin's into {@link Entity}.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
public interface EndimationEffectSource {
    /**
     * Gets the {@link Position} of this source.
     * <p>This gets used by effects that need to know where to process.</p>
     *
     * @return The {@link Position} of this source.
     */
    default Position getPos() {
        return Vec3d.ZERO;
    }

    /**
     * If this source is active.
     * <p>This gets used by effects that should not continue if their sources are not active.</p>
     *
     * @return If this source is active.
     */
    default boolean isActive() {
        return true;
    }
}
