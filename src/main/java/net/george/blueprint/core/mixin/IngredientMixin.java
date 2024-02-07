package net.george.blueprint.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.george.blueprint.core.api.recipe.CombinedIngredient;
import net.george.blueprint.core.api.recipe.CustomIngredient;
import net.george.blueprint.core.api.recipe.IngredientSerializer;
import net.george.blueprint.core.api.recipe.value.ValueSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = Ingredient.class, priority = 547)
public class IngredientMixin {
    @Shadow
    public static Ingredient.Entry entryFromJson(JsonObject json) {
        return null;
    }

    @Shadow
    public static Ingredient ofEntries(Stream<? extends Ingredient.Entry> entries) {
        return null;
    }

    @ModifyReturnValue(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    public boolean test(boolean itemMatches, @Nullable ItemStack itemStack) {
        if (this instanceof CustomIngredient custom) {
            if (custom.customTest()) {
                return custom.testCustom(itemStack, itemMatches);
            }
        }

        return itemMatches;
    }

    @Inject(method = "fromPacket", at = @At("HEAD"), cancellable = true)
    private static void fromPacket(PacketByteBuf buf, CallbackInfoReturnable<Ingredient> cir) {
        Ingredient deserialized = IngredientSerializer.tryDeserializeNetwork(buf);
        if (deserialized != null) {
            cir.setReturnValue(deserialized);
        }
    }

    /**
     * @author Mr.George
     * @reason To add custom {@link Ingredient} support.
     */
    @Overwrite
    public static Ingredient fromJson(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                Ingredient deserialized = IngredientSerializer.tryDeserializeJson(obj);
                return deserialized != null ? deserialized : ofEntries(Stream.of(entryFromJson(obj)));
            } else if (!json.isJsonArray()) {
                throw new JsonSyntaxException("Expected item to be object or array of objects");
            } else {
                JsonArray jsonArray = json.getAsJsonArray();
                if (jsonArray.size() == 0) {
                    throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
                } else {
                    List<Ingredient> nested = new ArrayList<>();

                    for (JsonElement element : jsonArray) {
                        nested.add(fromJson(element));
                    }

                    return nested.stream().allMatch((i) -> i.getClass() == Ingredient.class) ?
                            ofEntries(nested.stream().flatMap((i) -> Arrays.stream(i.entries))) :
                            new CombinedIngredient(nested);
                }
            }
        } else {
            throw new JsonSyntaxException("Item cannot be null");
        }
    }

    @Inject(method = "entryFromJson", at = @At("HEAD"), cancellable = true)
    private static void entryFromJson(JsonObject json, CallbackInfoReturnable<Ingredient.Entry> cir) {
        Ingredient.Entry deserialized = ValueSerializer.tryDeserializeJson(json);
        if (deserialized != null) {
            cir.setReturnValue(deserialized);
        }
    }
}
