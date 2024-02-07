package net.george.blueprint.core.api.conditions.config;

import net.george.blueprint.core.api.conditions.ConfigValueCondition;
import net.george.blueprint.core.api.config.ForgeConfigSpec;
import net.minecraft.util.Identifier;

/**
 * A predicate for a {@link ConfigValueCondition}, takes in a {@link ForgeConfigSpec.ConfigValue} and returns a boolean for whether it matches the condition.
 *
 * @author abigailfails
 */
public interface IConfigPredicate {
    /**
     * Gets the ID of the predicate that will be checked for when deserializing from JSON (e.g. {@code "blueprint:equals"}).
     *
     * @return A {@link Identifier} representing the predicate's unique identifier.
     */
    Identifier getId();

    /**
     * Takes in a {@link ForgeConfigSpec.ConfigValue} and returns true if it matches the predicate's condition.
     *
     * <p>As {@code value} can be of any type, if the predicate only works on a specific type it should throw an exception.
     * if {@code value} is the wrong type. However, if the serializer is written correctly it should detect this error first.</p>
     *
     * @param value The config value to check against.
     * @return Whether {@code value} meets the predicate.
     * @throws IllegalArgumentException If {@code value} is of an invalid type.
     */
    boolean test(ForgeConfigSpec.ConfigValue<?> value) throws IllegalArgumentException;
}
