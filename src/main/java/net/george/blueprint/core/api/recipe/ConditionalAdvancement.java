package net.george.blueprint.core.api.recipe;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConditionalAdvancement {
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated Please use {@linkplain #processConditional(JsonObject, ICondition.IContext) the more general overload}.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    @Nullable
    public static JsonObject processConditional(JsonObject json) {
        return processConditional(json, ICondition.IContext.EMPTY);
    }

    /**
     * Processes the conditional advancement during loading.
     * @param json The incoming json from the advancement file.
     * @return The advancement that passed the conditions, or null if none did.
     */
    @Nullable
    public static JsonObject processConditional(JsonObject json, ICondition.IContext context) {
        JsonArray entries = JsonHelper.getArray(json, "advancements", null);
        if (entries == null) {
            return CraftingHelper.processConditions(json, "conditions", context) ? json : null;
        }

        int index = 0;
        for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
                throw new JsonSyntaxException("Invalid advancement entry at index " + index + " must be JsonObject");
            }
            if (CraftingHelper.processConditions(JsonHelper.getArray(element.getAsJsonObject(), "conditions"), context)) {
                return JsonHelper.getObject(element.getAsJsonObject(), "advancement");
            }
            index++;
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private final List<ICondition[]> conditions = new ArrayList<>();
        private final List<Supplier<JsonElement>> advancements = new ArrayList<>();

        private final List<ICondition> currentConditions = new ArrayList<>();
        private boolean locked = false;

        @CanIgnoreReturnValue
        public Builder addCondition(ICondition condition) {
            if (this.locked) {
                throw new IllegalStateException("Attempted to modify finished builder");
            }
            this.currentConditions.add(condition);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder addAdvancement(Consumer<Consumer<Advancement.Builder>> callable) {
            if (this.locked) {
                throw new IllegalStateException("Attempted to modify finished builder");
            }
            callable.accept(this::addAdvancement);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder addAdvancement(Advancement.Builder advancement) {
            return addAdvancement(advancement::toJson);
        }

        @CanIgnoreReturnValue
        public Builder addAdvancement(RecipeJsonProvider fromRecipe) {
            return addAdvancement(fromRecipe::toAdvancementJson);
        }

        private Builder addAdvancement(Supplier<JsonElement> jsonSupplier) {
            if (this.locked) {
                throw new IllegalStateException("Attempted to modify finished builder");
            }
            if (this.currentConditions.isEmpty()) {
                throw new IllegalStateException("Can not add a advancement with no conditions.");
            }
            this.conditions.add(this.currentConditions.toArray(new ICondition[0]));
            this.advancements.add(jsonSupplier);
            this.currentConditions.clear();
            return this;
        }

        public JsonObject write() {
            if (!this.locked) {
                if (!this.currentConditions.isEmpty()) {
                    throw new IllegalStateException("Invalid builder state: Orphaned conditions");
                }
                if (this.advancements.isEmpty()) {
                    throw new IllegalStateException("Invalid builder state: No Advancements");
                }
                this.locked = true;
            }
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            json.add("advancements", array);
            for (int x = 0; x < this.conditions.size(); x++) {
                JsonObject holder = new JsonObject();

                JsonArray conditions = new JsonArray();
                for (ICondition condition : this.conditions.get(x)) {
                    conditions.add(CraftingHelper.serialize(condition));
                }
                holder.add("conditions", conditions);
                holder.add("advancement", this.advancements.get(x).get());

                array.add(holder);
            }
            return json;
        }
    }
}
