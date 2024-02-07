package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IntersectionIngredient extends BaseCustomIngredient {
    private final List<Ingredient> children;
    private ItemStack[] intersectedMatchingStacks = null;
    private IntList packedMatchingStacks = null;

    protected IntersectionIngredient(List<Ingredient> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException("Cannot create an Intersection Ingredient with one or no children");
        }
        this.children = Collections.unmodifiableList(children);
    }

    /**
     * Gets an intersection ingredient
     *
     * @param ingredients List of ingredients to match
     * @return Ingredient that only matches if all the passed ingredients match
     */
    public static Ingredient of(Ingredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an Intersection Ingredient with no children, use Ingredient.of() to create an empty ingredient");
        }
        if (ingredients.length == 1) {
            return ingredients[0];
        }

        return new IntersectionIngredient(Arrays.asList(ingredients));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        for (Ingredient ingredient : children) {
            if (!ingredient.test(stack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        if (this.intersectedMatchingStacks == null) {
            this.intersectedMatchingStacks = Arrays
                    .stream(children.get(0).getMatchingStacks())
                    .filter(stack -> {
                        for (int i = 1; i < children.size(); i++) {
                            if (!children.get(i).test(stack)) {
                                return false;
                            }
                        }
                        return true;
                    }).toArray(ItemStack[]::new);
        }
        return intersectedMatchingStacks;
    }

    @Override
    public boolean isEmpty() {
        return children.stream().anyMatch(Ingredient::isEmpty);
    }

    @Override
    public IntList getMatchingItemIds() {
        if (this.packedMatchingStacks == null) {
            ItemStack[] matchingStacks = getMatchingStacks();
            this.packedMatchingStacks = new IntArrayList(matchingStacks.length);
            for (ItemStack stack : matchingStacks)
                this.packedMatchingStacks.add(RecipeMatcher.getItemId(stack));
            this.packedMatchingStacks.sort(IntComparators.NATURAL_COMPARATOR);
        }
        return packedMatchingStacks;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Objects.requireNonNull(CraftingHelper.getId(Serializer.INSTANCE)).toString());
        JsonArray array = new JsonArray();
        for (Ingredient ingredient : children)
            array.add(ingredient.toJson());

        json.add("children", array);
        return json;
    }

    @Override
    public IngredientSerializer getDeserializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(Serializer.ID);
        buffer.writeVarInt(children.size());
        for (Ingredient ingredient : children) {
            ingredient.write(buffer);
        }
    }

    public static class Serializer implements IngredientSerializer {
        public static final Identifier ID = new Identifier(Blueprint.MOD_ID, "intersection");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Ingredient fromJson(JsonObject json) {
            JsonArray children = JsonHelper.getArray(json, "children");
            if (children.size() < 2)
                throw new JsonSyntaxException("Must have at least two children for an intersection ingredient");
            return new IntersectionIngredient(IntStream.range(0, children.size()).mapToObj(i -> Ingredient.fromJson(children.get(i))).toList());
        }

        @Override
        public Ingredient fromNetwork(PacketByteBuf buffer) {
            return new IntersectionIngredient(Stream.generate(() -> Ingredient.fromPacket(buffer)).limit(buffer.readVarInt()).toList());
        }
    }
}
