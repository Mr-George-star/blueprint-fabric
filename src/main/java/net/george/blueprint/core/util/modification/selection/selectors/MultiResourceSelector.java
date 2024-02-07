package net.george.blueprint.core.util.modification.selection.selectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.george.blueprint.core.util.modification.selection.ConditionedResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelectorSerializers;
import net.george.blueprint.core.util.modification.selection.SelectionSpace;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link ResourceSelector} implementation that acts as multiple {@link ConditionedResourceSelector} instances.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record MultiResourceSelector(List<ConditionedResourceSelector> selectors) implements ResourceSelector<MultiResourceSelector> {
    public MultiResourceSelector(ConditionedResourceSelector... selectors) {
        this(List.of(selectors));
    }

    public MultiResourceSelector(ResourceSelector<?>... selectors) {
        this(Stream.of(selectors).map(ConditionedResourceSelector::new).toList());
    }

    public MultiResourceSelector(List<ConditionedResourceSelector> selectors) {
        this.selectors = selectors;
    }

    public List<Identifier> select(SelectionSpace space) {
        List<Identifier> targetNames = new ArrayList<>();
        this.selectors.forEach((configuredModifierTargetSelector) ->
                targetNames.addAll(configuredModifierTargetSelector.select(space)));
        return targetNames;
    }

    public Serializer getSerializer() {
        return ResourceSelectorSerializers.MULTI;
    }

    public List<ConditionedResourceSelector> selectors() {
        return this.selectors;
    }

    /**
     * The serializer class for the {@link MultiResourceSelector}.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public static final class Serializer implements ResourceSelector.Serializer<MultiResourceSelector> {
        public Serializer() {
        }

        public JsonElement serialize(MultiResourceSelector selector) {
            JsonArray jsonArray = new JsonArray();
            selector.selectors.forEach((conditionedModifierTargetSelector) ->
                    jsonArray.add(conditionedModifierTargetSelector.serialize()));
            return jsonArray;
        }

        public MultiResourceSelector deserialize(JsonElement element) {
            JsonArray jsonArray = element.getAsJsonArray();
            List<ConditionedResourceSelector> targetSelectors = new ArrayList<>(jsonArray.size());
            jsonArray.forEach((entry) ->
                    targetSelectors.add(ConditionedResourceSelector.deserialize(entry.toString(), entry)));
            return new MultiResourceSelector(targetSelectors);
        }
    }
}