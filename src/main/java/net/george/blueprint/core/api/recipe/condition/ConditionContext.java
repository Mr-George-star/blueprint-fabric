package net.george.blueprint.core.api.recipe.condition;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ConditionContext implements ICondition.IContext {
    private final TagManagerLoader tagManager;
    private Map<RegistryKey<?>, Map<Identifier, Tag<RegistryEntry<?>>>> loadedTags = null;

    public ConditionContext(TagManagerLoader tagManager) {
        this.tagManager = tagManager;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Map<Identifier, Tag<RegistryEntry<T>>> getAllTags(RegistryKey<? extends Registry<T>> registry) {
        if (loadedTags == null) {
            List<TagManagerLoader.RegistryTags<?>> tags = tagManager.getRegistryTags();
            if (tags.isEmpty()) {
                throw new IllegalStateException("Tags have not been loaded yet.");
            }

            loadedTags = new IdentityHashMap<>();
            for (TagManagerLoader.RegistryTags<?> loadResult : tags) {
                loadedTags.put(loadResult.key(),  (Map) Collections.unmodifiableMap(loadResult.tags()));
            }
        }
        return (Map) loadedTags.getOrDefault(registry, Collections.emptyMap());
    }
}
