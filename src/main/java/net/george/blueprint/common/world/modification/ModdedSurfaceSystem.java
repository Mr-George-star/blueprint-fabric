package net.george.blueprint.common.world.modification;

import net.george.blueprint.core.mixin.extension.SurfaceBuilderMixin;

import javax.annotation.Nullable;

/**
 * The interface used for adding methods to handle storage of {@link ModdedBiomeSource} instances in {@link net.minecraft.world.gen.surfacebuilder.SurfaceBuilder} instances.
 *
 * @author SmellyModder (Luke Tonon)
 * @see SurfaceBuilderMixin
 */
public interface ModdedSurfaceSystem {
    /**
     * Gets the {@link ModdedBiomeSource} instance being stored.
     *
     * @return The {@link ModdedBiomeSource} instance being stored.
     */
    @Nullable
    ModdedBiomeSource getModdedBiomeSource();

    /**
     * Sets the source.
     *
     * @param moddedBiomeSource The new {@link ModdedBiomeSource} instance to use.
     */
    void setModdedBiomeSource(@Nullable ModdedBiomeSource moddedBiomeSource);
}
