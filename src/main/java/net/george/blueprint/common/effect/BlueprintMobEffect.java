package net.george.blueprint.common.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * A {@link StatusEffect} extension used to get access to the protected constructor in the {@link StatusEffect} class.
 */
public class BlueprintMobEffect extends StatusEffect {
    public BlueprintMobEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
