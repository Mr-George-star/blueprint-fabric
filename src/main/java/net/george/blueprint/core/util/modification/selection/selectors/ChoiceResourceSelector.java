package net.george.blueprint.core.util.modification.selection.selectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.george.blueprint.core.api.recipe.condition.FalseCondition;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.util.modification.selection.ConditionedResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelectorSerializers;
import net.george.blueprint.core.util.modification.selection.SelectionSpace;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.List;

/**
 * A {@link ResourceSelector} implementation that picks a {@link ConditionedResourceSelector} if a condition is met or picks another {@link ConditionedResourceSelector} if the condition is not met.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record ChoiceResourceSelector(ConditionedResourceSelector first, ConditionedResourceSelector second, ICondition condition) implements ResourceSelector<ChoiceResourceSelector> {
    public ChoiceResourceSelector(ResourceSelector<?> first, ResourceSelector<?> second, ICondition condition) {
        this(new ConditionedResourceSelector(first), new ConditionedResourceSelector(second), condition);
    }

    public ChoiceResourceSelector(ConditionedResourceSelector first, ConditionedResourceSelector second, ICondition condition) {
        this.first = first;
        this.second = second;
        this.condition = condition;
    }

    public List<Identifier> select(SelectionSpace space) {
        return this.condition.test() ? this.first.select(space) : this.second.select(space);
    }

    public Serializer getSerializer() {
        return ResourceSelectorSerializers.CHOICE;
    }

    public ConditionedResourceSelector first() {
        return this.first;
    }

    public ConditionedResourceSelector second() {
        return this.second;
    }

    public ICondition condition() {
        return this.condition;
    }

    /**
     * The serializer class for the {@link ChoiceResourceSelector}.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public static final class Serializer implements ResourceSelector.Serializer<ChoiceResourceSelector> {
        public Serializer() {
        }

        public JsonElement serialize(ChoiceResourceSelector selector) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("first", selector.first.serialize());
            jsonObject.add("second", selector.second.serialize());
            jsonObject.add("condition", CraftingHelper.serialize(selector.condition));
            return jsonObject;
        }

        public ChoiceResourceSelector deserialize(JsonElement element) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonObject conditionObject = JsonHelper.asObject(jsonObject.get("condition"), "condition");

            ICondition condition;
            try {
                condition = CraftingHelper.getCondition(conditionObject);
            } catch (JsonSyntaxException exception) {
                return new ChoiceResourceSelector(ConditionedResourceSelector.EMPTY, ConditionedResourceSelector.deserialize("second", JsonHelper.asObject(jsonObject.get("second"), "second")), FalseCondition.INSTANCE);
            }

            return new ChoiceResourceSelector(ConditionedResourceSelector.deserialize("first", jsonObject.get("first")), ConditionedResourceSelector.deserialize("second", jsonObject.get("second")), condition);
        }
    }
}
