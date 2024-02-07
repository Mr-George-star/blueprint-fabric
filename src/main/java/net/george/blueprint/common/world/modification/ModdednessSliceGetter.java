package net.george.blueprint.common.world.modification;

import net.george.blueprint.core.mixin.extension.MaterialRuleContextMixin;
import net.george.blueprint.core.registry.BlueprintSurfaceRules;
import net.minecraft.util.Identifier;

/**
 * The interface used for adding certain methods used by {@link BlueprintSurfaceRules.ModdednessSliceConditionSource} instances.
 *
 * @author SmellyModder (Luke Tonon)
 * @see MaterialRuleContextMixin
 */
public interface ModdednessSliceGetter {
    /**
     * Checks if this getter cannot process {@link #getSliceName()} successfully.
     *
     * @return If this getter cannot process {@link #getSliceName()} successfully.
     */
    boolean cannotGetSlices();

    /**
     * Gets the name of a modded provider at the current position.
     *
     * @return The name of a modded provider at the current position.
     */
    Identifier getSliceName();
}
