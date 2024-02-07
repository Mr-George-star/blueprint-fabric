package net.george.blueprint.core.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import javax.annotation.Nullable;

/**
 * This interface should be implemented onto all custom {@link Ingredient}s.
 * This is required for your ingredient to function correctly.
 */
@SuppressWarnings("unused")
public interface CustomIngredient {
    /**
     * @return the custom {@link IngredientSerializer} used by this ingredient, or null
     * if vanilla deserialization should be used.
     */
    IngredientSerializer getDeserializer();

    /**
     * @return if this {@link Ingredient} should have custom logic for testing if an {@link ItemStack} matches it
     */
    default boolean customTest() {
        return false;
    }

    /**
     * Convenience method for adding custom logic for matching.
     * In order for this to be used, {@link #customTest()} must return true.
     * @param itemMatches if true, the given {@link ItemStack}'s Item matches this {@link Ingredient} already.
     * @return if the given {@link ItemStack} matches this {@link Ingredient}.
     */
    default boolean testCustom(@Nullable ItemStack stack, boolean itemMatches) {
        throw new IllegalStateException("May never be called if custom test return false");
    }

    default boolean customDeserializer() {
        return getDeserializer() != null;
    }

    static boolean customDeserializer(Ingredient ingredient) {
        return ingredient instanceof CustomIngredient customIngredient && customIngredient.customDeserializer();
    }
}
