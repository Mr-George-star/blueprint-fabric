package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonElement;
import net.minecraft.recipe.Ingredient;

import java.util.stream.Stream;

/**
 * Extension of {@link Ingredient} which makes most methods custom ingredients need to implement abstract, and removes the static constructors.
 * Mods are encouraged to extend this class for their custom ingredients.
 */
public abstract class BaseCustomIngredient extends Ingredient implements CustomIngredient {
    /**
     * Value constructor, for ingredients that have some vanilla representation
     */
    protected BaseCustomIngredient(Stream<? extends Entry> stream) {
        super(stream);
    }

    /**
     * Empty constructor, for the sake of dynamic ingredients.
     */
    protected BaseCustomIngredient() {
        this(Stream.of());
    }

    @Override
    public abstract JsonElement toJson();
}
