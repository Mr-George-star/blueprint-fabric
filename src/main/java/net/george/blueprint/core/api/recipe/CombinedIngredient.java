package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link Ingredient} that wraps several children {@link Ingredient}s, matching if any of them match
 */
public class CombinedIngredient extends BaseCustomIngredient {
    public final List<Ingredient> children;
    private final ItemStack[] items;
    private final IntList stackingIds;
    private final boolean empty;

    public CombinedIngredient(List<Ingredient> children) {
        this.children = children;
        this.items = children.stream().flatMap(ingredient -> Arrays.stream(ingredient.getMatchingStacks())).toArray(ItemStack[]::new);
        this.stackingIds = new IntArrayList(children.stream().flatMapToInt(ingredient -> ingredient.getMatchingItemIds().intStream()).toArray());
        this.empty = children.stream().allMatch(Ingredient::isEmpty);
    }

    public CombinedIngredient(Ingredient... children) {
        this(List.of(children));
    }

    @Override
    public IngredientSerializer getDeserializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        for (Ingredient child : children) {
            if (child.test(itemStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        return items;
    }

    @Override
    public IntList getMatchingItemIds() {
        return stackingIds;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(Serializer.ID);
        buffer.writeVarInt(children.size());
        for (Ingredient child : children) {
            child.write(buffer);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonArray children = new JsonArray();
        for (Ingredient child : this.children) {
            children.add(child.toJson());
        }
        return children;
    }

    public static class Serializer implements IngredientSerializer {
        public static final Identifier ID = new Identifier(Blueprint.MOD_ID, "combined");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Ingredient fromNetwork(PacketByteBuf buffer) {
            int count = buffer.readVarInt();
            List<Ingredient> children = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                children.add(Ingredient.fromPacket(buffer));
            }
            return new CombinedIngredient(children);
        }

        @Override
        public Ingredient fromJson(JsonObject object) {
            JsonElement childrenElement = object.get("children");
            if (!(childrenElement instanceof JsonArray array)) {
                throw new JsonSyntaxException("Combined Ingredient expected json element to be an array, found: " + childrenElement);
            }
            List<Ingredient> children = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                children.add(Ingredient.fromJson(array.get(i)));
            }
            return new CombinedIngredient(children);
        }
    }
}
