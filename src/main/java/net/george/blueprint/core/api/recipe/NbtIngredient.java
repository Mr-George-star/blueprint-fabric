package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.george.blueprint.core.Blueprint;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * {@link Ingredient} that matches the given stack, performing an exact {@link NbtCompound} match.
 */
public class NbtIngredient extends BaseCustomIngredient {
    private final ItemStack stack;

    protected NbtIngredient(ItemStack stack) {
        super(Stream.of(new Ingredient.StackEntry(stack)));
        this.stack = stack;
    }

    /**
     * Creates a new ingredient matching the given stack and tag
     */
    public static NbtIngredient of(ItemStack stack) {
        return new NbtIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        return this.stack.getItem() == input.getItem() && this.stack.getDamage() == input.getDamage() && CraftingHelper.areShareTagsEqual(this.stack, input);
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", IngredientSerializer.REGISTRY.getKey(Serializer.INSTANCE).toString());
        json.addProperty("item", Registry.ITEM.getKey(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        if (stack.hasNbt())
            json.addProperty("nbt", Objects.requireNonNull(stack.getNbt()).toString());
        return json;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(Serializer.ID);
        buffer.writeItemStack(stack);
    }

    @Override
    public IngredientSerializer getDeserializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IngredientSerializer {
        public static final Identifier ID = new Identifier(Blueprint.MOD_ID, "nbt");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Ingredient fromNetwork(PacketByteBuf buffer) {
            return new NbtIngredient(buffer.readItemStack());
        }

        @Nullable
        @Override
        public Ingredient fromJson(JsonObject object) {
            return new NbtIngredient(CraftingHelper.getItemStack(object.getAsJsonObject(), true));
        }
    }
}
