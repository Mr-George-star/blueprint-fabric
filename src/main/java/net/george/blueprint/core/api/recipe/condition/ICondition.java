package net.george.blueprint.core.api.recipe.condition;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Collections;
import java.util.Map;

public interface ICondition {
    Identifier getId();

    default boolean test(IContext context) {
        return test();
    }

    boolean test();

    interface IContext {
        IContext EMPTY = new IContext() {
            @Override
            public <T> Map<Identifier, Tag<RegistryEntry<T>>> getAllTags(RegistryKey<? extends Registry<T>> registry) {
                return Collections.emptyMap();
            }
        };

        /**
         * Return the requested tag if available, or an empty tag otherwise.
         */
        default <T> Tag<RegistryEntry<T>> getTag(TagKey<T> key) {
            return getAllTags(key.registry()).getOrDefault(key.id(), Tag.empty());
        }

        /**
         * Return all the loaded tags for the passed registry, or an empty map if none is available.
         * Note that the map and the tags are unmodifiable.
         */
        <T> Map<Identifier, Tag<RegistryEntry<T>>> getAllTags(RegistryKey<? extends Registry<T>> registry);
    }
}
