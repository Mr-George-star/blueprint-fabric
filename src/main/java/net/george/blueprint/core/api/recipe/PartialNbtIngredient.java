package net.george.blueprint.core.api.recipe;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Ingredient} that matches the given items, performing a partial NBT match. Use {@link NbtIngredient} if you want exact match on NBT
 */
public class PartialNbtIngredient extends BaseCustomIngredient {
    private final Set<Item> items;
    private final NbtCompound nbt;
    private final NbtPredicate predicate;

    protected PartialNbtIngredient(Set<Item> items, NbtCompound nbt) {
        super(items.stream().map(item -> {
            ItemStack stack = new ItemStack(item);
            stack.setNbt(nbt.copy());
            return new Ingredient.StackEntry(stack);
        }));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a PartialNBTIngredient with no items");
        }
        this.items = Collections.unmodifiableSet(items);
        this.nbt = nbt;
        this.predicate = new NbtPredicate(nbt);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given Nbt
     */
    public static PartialNbtIngredient of(NbtCompound nbt, ItemConvertible... items) {
        return new PartialNbtIngredient(Arrays.stream(items).map(ItemConvertible::asItem).collect(Collectors.toSet()), nbt);
    }

    /**
     * Creates a new ingredient matching the given item, containing the given Nbt
     */
    public static PartialNbtIngredient of(ItemConvertible item, NbtCompound nbt) {
        return new PartialNbtIngredient(Set.of(item.asItem()), nbt);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        return items.contains(input.getItem()) && predicate.test(input.getNbt());
    }

    @Override
    public IngredientSerializer getDeserializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Objects.requireNonNull(CraftingHelper.getId(Serializer.INSTANCE)).toString());
        if (items.size() == 1) {
            json.addProperty("item", Registry.ITEM.getKey(items.iterator().next()).toString());
        } else {
            JsonArray items = new JsonArray();
            this.items.stream().map(Registry.ITEM::getKey).sorted().forEach(name -> items.add(name.toString()));
            json.add("items", items);
        }
        json.addProperty("nbt", nbt.toString());
        return json;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(Serializer.ID);
        buffer.writeVarInt(items.size());
        for (Item item : items) {
            buffer.writeIdentifier(Registry.ITEM.getId(item));
        }
        buffer.writeNbt(nbt);
    }

    public static class Serializer implements IngredientSerializer {
        public static final Identifier ID = new Identifier(Blueprint.MOD_ID, "partial_nbt");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Ingredient fromJson(JsonObject json) {
            Set<Item> items;
            if (json.has("item"))
                items = Set.of(CraftingHelper.getItem(JsonHelper.getString(json, "item"), true));
            else if (json.has("items")) {
                ImmutableSet.Builder<Item> builder = ImmutableSet.builder();
                JsonArray itemArray = JsonHelper.getArray(json, "items");
                for (int i = 0; i < itemArray.size(); i++) {
                    builder.add(CraftingHelper.getItem(JsonHelper.asString(itemArray.get(i), "items[" + i + ']'), true));
                }
                items = builder.build();
            } else
                throw new JsonSyntaxException("Must set either 'item' or 'items'");

            if (!json.has("nbt")) {
                throw new JsonSyntaxException("Missing nbt, expected to find a String or JsonObject");
            }
            NbtCompound nbt = CraftingHelper.getNbt(json.get("nbt"));

            return new PartialNbtIngredient(items, nbt);
        }

        @Override
        public Ingredient fromNetwork(PacketByteBuf buffer) {
            Set<Item> items = Stream.generate(() -> Registry.ITEM.get(buffer.readIdentifier())).limit(buffer.readVarInt()).collect(Collectors.toSet());
            NbtCompound nbt = buffer.readNbt();
            return new PartialNbtIngredient(items, Objects.requireNonNull(nbt));
        }
    }
}
