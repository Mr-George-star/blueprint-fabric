package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * {@link Ingredient} that matches everything from the first ingredient that is not included in the second ingredient
 */
public class DifferenceIngredient extends BaseCustomIngredient {
    private final Ingredient base;
    private final Ingredient subtracted;
    private ItemStack[] filteredMatchingStacks;
    private IntList packedMatchingStacks;

    protected DifferenceIngredient(Ingredient base, Ingredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    /**
     * Gets the difference from the two ingredients
     *
     * @param base       Ingredient the item must match
     * @param subtracted Ingredient the item must not match
     * @return {@link Ingredient} that {@code base} anything in base that is not in {@code subtracted}
     */
    public static DifferenceIngredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        if (this.filteredMatchingStacks == null)
            this.filteredMatchingStacks = Arrays.stream(base.getMatchingStacks())
                    .filter(stack -> !subtracted.test(stack))
                    .toArray(ItemStack[]::new);
        return filteredMatchingStacks;
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
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
        json.add("base", base.toJson());
        json.add("subtracted", subtracted.toJson());
        return json;
    }

    @Override
    public IngredientSerializer getDeserializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(Serializer.ID);
        base.write(buffer);
        subtracted.write(buffer);
    }

    public static class Serializer implements IngredientSerializer {
        public static final Identifier ID = new Identifier(Blueprint.MOD_ID, "difference");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Ingredient fromJson(JsonObject json) {
            Ingredient base = Ingredient.fromJson(json.get("base"));
            Ingredient without = Ingredient.fromJson(json.get("subtracted"));
            return new DifferenceIngredient(base, without);
        }

        @Override
        public Ingredient fromNetwork(PacketByteBuf buffer) {
            Ingredient base = Ingredient.fromPacket(buffer);
            Ingredient without = Ingredient.fromPacket(buffer);
            return new DifferenceIngredient(base, without);
        }
    }
}
