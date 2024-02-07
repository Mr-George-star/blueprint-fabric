package net.george.blueprint.core.api.recipe.value;

import net.minecraft.recipe.Ingredient;

/**
 * A custom {@link Ingredient.Entry}. Should be implemented onto all custom {@link Ingredient.Entry}s.
 */
public interface CustomValue {
    /**
     * @return the custom {@link ValueSerializer} used by this value, or null
     * if vanilla deserialization should be used.
     */
    ValueSerializer getDeserializer();

    default boolean customDeserializer() {
        return this.getDeserializer() != null;
    }

    static boolean customDeserializer(Ingredient.Entry value) {
        if (value instanceof CustomValue customValue) {
            return customValue.customDeserializer();
        }
        return false;
    }
}
