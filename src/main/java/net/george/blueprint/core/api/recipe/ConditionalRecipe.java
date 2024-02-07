package net.george.blueprint.core.api.recipe;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ConditionalRecipe {
    public static final RecipeSerializer<Recipe<?>> SERIALIZER = null;

    public static Builder builder() {
        return new Builder();
    }

    public static class Serializer<T extends Recipe<?>> implements RecipeSerializer<T> {
        private Identifier name;

        @SuppressWarnings("unchecked")
        private static <G> Class<G> castClass(Class<?> clazz) {
            return (Class<G>)clazz;
        }

        @Override
        public T read(Identifier recipeId, JsonObject json) {
            return read(recipeId, json, ICondition.IContext.EMPTY);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(Identifier recipeId, JsonObject json, ICondition.IContext context) {
            JsonArray items = JsonHelper.getArray(json, "recipes");
            int index = 0;
            for (JsonElement element : items) {
                if (!element.isJsonObject()) {
                    throw new JsonSyntaxException("Invalid recipes entry at index " + index + " Must be JsonObject");
                }
                if (CraftingHelper.processConditions(JsonHelper.getArray(element.getAsJsonObject(), "conditions"), context)) {
                    return (T) RecipeManager.deserialize(recipeId, JsonHelper.getObject(element.getAsJsonObject(), "recipe"));
                }
                index++;
            }
            return null;
        }

        //Should never get here as we return one of the recipes we wrap.
        @Override
        public T read(Identifier recipeId, PacketByteBuf buffer) {
            return null;
        }

        @Override
        public void write(PacketByteBuf buffer, T recipe) {}
    }

    public static class Builder {
        private final List<ICondition[]> conditions = new ArrayList<>();
        private final List<RecipeJsonProvider> recipes = new ArrayList<>();
        private Identifier advancementId;
        private ConditionalAdvancement.Builder advancementBuilder;

        private final List<ICondition> currentConditions = new ArrayList<>();

        public Builder addCondition(ICondition condition) {
            this.currentConditions.add(condition);
            return this;
        }

        public Builder addRecipe(Consumer<Consumer<RecipeJsonProvider>> callable) {
            callable.accept(this::addRecipe);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder addRecipe(RecipeJsonProvider recipe) {
            if (this.currentConditions.isEmpty()) {
                throw new IllegalStateException("Can not add a recipe with no conditions.");
            }
            this.conditions.add(this.currentConditions.toArray(new ICondition[0]));
            this.recipes.add(recipe);
            this.currentConditions.clear();
            return this;
        }

        public Builder generateAdvancement() {
            return generateAdvancement(null);
        }

        public Builder generateAdvancement(@Nullable Identifier id) {
            ConditionalAdvancement.Builder builder = ConditionalAdvancement.builder();
            for(int i = 0; i < this.recipes.size(); i++) {
                for(ICondition condition : this.conditions.get(i)) {
                    builder = builder.addCondition(condition);
                }
                builder = builder.addAdvancement(this.recipes.get(i));
            }
            return setAdvancement(id, builder);
        }

        public Builder setAdvancement(ConditionalAdvancement.Builder advancement) {
            return setAdvancement(null, advancement);
        }

        public Builder setAdvancement(String namespace, String path, ConditionalAdvancement.Builder advancement) {
            return setAdvancement(new Identifier(namespace, path), advancement);
        }

        public Builder setAdvancement(@Nullable Identifier id, ConditionalAdvancement.Builder advancement) {
            if (this.advancementBuilder != null) {
                throw new IllegalStateException("Invalid ConditionalRecipeBuilder, Advancement already set");
            }
            this.advancementId = id;
            this.advancementBuilder = advancement;
            return this;
        }

        public void build(Consumer<RecipeJsonProvider> consumer, String namespace, String path) {
            build(consumer, new Identifier(namespace, path));
        }

        public void build(Consumer<RecipeJsonProvider> consumer, Identifier id) {
            if (!this.currentConditions.isEmpty()) {
                throw new IllegalStateException("Invalid ConditionalRecipe builder, Orphaned conditions");
            }
            if (this.recipes.isEmpty()) {
                throw new IllegalStateException("Invalid ConditionalRecipe builder, No recipes");
            }

            if (this.advancementId == null && this.advancementBuilder != null) {
                this.advancementId = new Identifier(id.getNamespace(), "recipes/" + id.getPath());
            }

            consumer.accept(new Finished(id, this.conditions, this.recipes, this.advancementId, this.advancementBuilder));
        }
    }

    private static class Finished implements RecipeJsonProvider {
        private final Identifier id;
        private final List<ICondition[]> conditions;
        private final List<RecipeJsonProvider> recipes;
        private final Identifier advancementId;
        private final ConditionalAdvancement.Builder advancement;

        private Finished(Identifier id, List<ICondition[]> conditions, List<RecipeJsonProvider> recipes, @Nullable Identifier advancementId, @Nullable ConditionalAdvancement.Builder advancement) {
            this.id = id;
            this.conditions = conditions;
            this.recipes = recipes;
            this.advancementId = advancementId;
            this.advancement = advancement;
        }

        @Override
        public void serialize(JsonObject json) {
            JsonArray array = new JsonArray();
            json.add("recipes", array);
            for (int x = 0; x < this.conditions.size(); x++) {
                JsonObject holder = new JsonObject();

                JsonArray conditions = new JsonArray();
                for (ICondition condition : this.conditions.get(x))
                    conditions.add(CraftingHelper.serialize(condition));
                holder.add("conditions", conditions);
                holder.add("recipe", this.recipes.get(x).toJson());

                array.add(holder);
            }
        }

        @Override
        public Identifier getRecipeId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return SERIALIZER;
        }

        @Override
        public JsonObject toAdvancementJson() {
            return this.advancement == null ? null : this.advancement.write();
        }

        @Override
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}
