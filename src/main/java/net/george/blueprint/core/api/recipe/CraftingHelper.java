package net.george.blueprint.core.api.recipe;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.recipe.condition.*;
import net.george.blueprint.core.api.recipe.value.ValueSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableProvider.GSON;

@SuppressWarnings("unused")
public class CraftingHelper {
    public static void init() {
        ValueSerializer.init();

        register(CombinedIngredient.Serializer.ID, CombinedIngredient.Serializer.INSTANCE);
        register(NbtIngredient.Serializer.ID, NbtIngredient.Serializer.INSTANCE);
        register(DifferenceIngredient.Serializer.ID, DifferenceIngredient.Serializer.INSTANCE);
        register(IntersectionIngredient.Serializer.ID, IntersectionIngredient.Serializer.INSTANCE);
        register(PartialNbtIngredient.Serializer.ID, PartialNbtIngredient.Serializer.INSTANCE);

        register(AndCondition.Serializer.INSTANCE);
        register(OrCondition.Serializer.INSTANCE);
        register(FalseCondition.Serializer.INSTANCE);
        register(TrueCondition.Serializer.INSTANCE);
        register(ItemExistsCondition.Serializer.INSTANCE);
        register(ModLoadedCondition.Serializer.INSTANCE);
        register(TagEmptyCondition.Serializer.INSTANCE);
        register(NotCondition.Serializer.INSTANCE);

        register(new Identifier(Blueprint.MOD_ID, "conditional_recipe"), new ConditionalRecipe.Serializer<>());
    }

    @CanIgnoreReturnValue
    public static IngredientSerializer register(Identifier id, IngredientSerializer serializer) {
        return Registry.register(IngredientSerializer.REGISTRY, id, serializer);
    }

    @CanIgnoreReturnValue
    public static IConditionSerializer<?> register(IConditionSerializer<?> serializer) {
        return Registry.register(IConditionSerializer.REGISTRY, serializer.getId(), serializer);
    }

    @CanIgnoreReturnValue
    public static RecipeSerializer<?> register(Identifier id, RecipeSerializer<?> serializer) {
        return Registry.register(Registry.RECIPE_SERIALIZER, id, serializer);
    }

    @CanIgnoreReturnValue
    public static ValueSerializer register(Identifier id, ValueSerializer serializer) {
        return Registry.register(ValueSerializer.REGISTRY, id, serializer);
    }

    @Nullable
    public static Identifier getId(IngredientSerializer serializer) {
        return IngredientSerializer.REGISTRY.getId(serializer);
    }

    @Nullable
    public static Identifier getId(IConditionSerializer<?> serializer) {
        return IConditionSerializer.REGISTRY.getId(serializer);
    }

    @Nullable
    public static Identifier getId(RecipeSerializer<?> serializer) {
        return Registry.RECIPE_SERIALIZER.getId(serializer);
    }

    @Nullable
    public static Identifier getId(ValueSerializer serializer) {
        return ValueSerializer.REGISTRY.getId(serializer);
    }

    public static ItemStack getItemStack(JsonObject json, boolean readNbt) {
        return getItemStack(json, readNbt, false);
    }

    public static Item getItem(String itemName, boolean disallowsAirInRecipe) {
        Item item = tryGetItem(itemName, disallowsAirInRecipe);
        if (item == null) {
            if (!Registry.ITEM.containsId(new Identifier(itemName))) {
                throw new JsonSyntaxException("Unknown item '" + itemName + "'");
            }
            if (disallowsAirInRecipe && item == Items.AIR) {
                throw new JsonSyntaxException("Invalid item: " + itemName);
            }
        }
        return Objects.requireNonNull(item);
    }

    @Nullable
    public static Item tryGetItem(String itemName, boolean disallowsAirInRecipe) {
        Identifier itemKey = new Identifier(itemName);
        if (!Registry.ITEM.containsId(itemKey)) {
            return null;
        }

        Item item = Registry.ITEM.get(itemKey);
        if (disallowsAirInRecipe && item == Items.AIR) {
            return null;
        }
        return item;
    }

    public static NbtCompound getNbt(JsonElement element) {
        try {
            if (element.isJsonObject()) {
                return StringNbtReader.parse(GSON.toJson(element));
            } else {
                return StringNbtReader.parse(JsonHelper.asString(element, "nbt"));
            }
        } catch (CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid Nbt Entry: " + exception);
        }
    }

    @Nullable
    public static NbtCompound tryGetNbt(JsonElement element) {
        try {
            if (element.isJsonObject()) {
                return StringNbtReader.parse(GSON.toJson(element));
            } else {
                return StringNbtReader.parse(JsonHelper.asString(element, "nbt"));
            }
        } catch (CommandSyntaxException exception) {
            return null;
        }
    }

    public static ItemStack getItemStack(JsonObject json, boolean readNBT, boolean disallowsAirInRecipe) {
        String itemName = JsonHelper.getString(json, "item");
        Item item = getItem(itemName, disallowsAirInRecipe);
        if (readNBT && json.has("nbt")) {
            NbtCompound nbt = getNbt(json.get("nbt"));
            NbtCompound temp = new NbtCompound();

            temp.put("tag", nbt);
            temp.putString("id", itemName);
            temp.putInt("Count", JsonHelper.getInt(json, "count", 1));

            return ItemStack.fromNbt(temp);
        }

        return new ItemStack(item, JsonHelper.getInt(json, "count", 1));
    }

    @Nullable
    public static ItemStack tryGetItemStack(JsonObject json, boolean readNBT, boolean disallowsAirInRecipe) {
        JsonElement nameElement = json.get("name");
        if (nameElement == null || !nameElement.isJsonPrimitive()) {
            return null;
        }
        String itemName = nameElement.getAsString();
        Item item = tryGetItem(itemName, disallowsAirInRecipe);
        if (readNBT && json.has("nbt")) {
            NbtCompound nbt = tryGetNbt(json.get("nbt"));
            if (nbt == null) {
                return null;
            }
            NbtCompound temp = new NbtCompound();

            temp.put("tag", nbt);
            temp.putString("id", itemName);
            temp.putInt("Count", JsonHelper.getInt(json, "count", 1));

            return ItemStack.fromNbt(temp);
        }

        return new ItemStack(item, JsonHelper.getInt(json, "count", 1));
    }

    /**
     * Merges several vanilla Ingredients together. As a quirk of how the json is structured, we can't tell if it's a single Ingredient type or multiple, so we split per item and re-merge here.
     * Only public for internal use, so we can access a private field in here.
     */
    public static Ingredient merge(Collection<Ingredient> parts) {
        return Ingredient.ofEntries(parts.stream().flatMap(i -> Arrays.stream(i.entries)));
    }

    /**
     * Modeled after ItemStack.areItemStackTagsEqual
     * Uses Item.getNBTShareTag for comparison instead of NBT and capabilities.
     * Only used for comparing itemStacks that were transferred from server to client using Item.getNBTShareTag.
     */
    public static boolean areShareTagsEqual(ItemStack stack, ItemStack other) {
        NbtCompound shareTagA = stack.getNbt();
        NbtCompound  shareTagB = other.getNbt();
        if (shareTagA == null) {
            return shareTagB == null;
        } else {
            return shareTagA.equals(shareTagB);
        }
    }


    /**
     * @deprecated Please use the {@linkplain #processConditions(JsonObject, String, ICondition.IContext) other more general overload}.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static boolean processConditions(JsonObject json, String memberName) {
        return processConditions(json, memberName, ICondition.IContext.EMPTY);
    }

    public static boolean processConditions(JsonObject json, String memberName, ICondition.IContext context) {
        return !json.has(memberName) || processConditions(JsonHelper.getArray(json, memberName), context);
    }

    public static boolean processConditions(JsonArray conditions) {
        return processConditions(conditions, ICondition.IContext.EMPTY);
    }

    public static boolean processConditions(JsonArray conditions, ICondition.IContext context) {
        for (int x = 0; x < conditions.size(); x++) {
            if (!conditions.get(x).isJsonObject()) {
                throw new JsonSyntaxException("Conditions must be an array of JsonObjects");
            }

            JsonObject json = conditions.get(x).getAsJsonObject();
            if (!CraftingHelper.getCondition(json).test(context)) {
                return false;
            }
        }
        return true;
    }

    public static ICondition getCondition(JsonObject json) {
        Identifier type = new Identifier(JsonHelper.getString(json, "type"));
        IConditionSerializer<?> serializer = IConditionSerializer.REGISTRY.get(type);
        if (serializer == null) {
            throw new JsonSyntaxException("Unknown condition type: " + type);
        }
        return serializer.read(json);
    }

    public static <T extends ICondition> JsonObject serialize(T condition) {
        @SuppressWarnings("unchecked")
        IConditionSerializer<T> serializer = IConditionSerializer.REGISTRY.get(condition.getId());
        if (serializer == null) {
            throw new JsonSyntaxException("Unknown condition type: " + condition.getId().toString());
        }
        return serializer.getJson(condition);
    }
}
