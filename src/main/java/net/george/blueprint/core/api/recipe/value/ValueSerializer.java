package net.george.blueprint.core.api.recipe.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.george.blueprint.core.Blueprint;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

/**
 * A {@link ValueSerializer} handles converting Json back to a {@link Ingredient.Entry}.
 */
@SuppressWarnings("unused")
public interface ValueSerializer {
    /**
     * The {@link Registry} for serializers.
     */
    Registry<ValueSerializer> REGISTRY = FabricRegistryBuilder.createSimple(
            ValueSerializer.class, new Identifier(Blueprint.MOD_ID, "value_serializer")
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    /**
     * Create a Value from the given json object.
     * This should reflect the corresponding {@link Ingredient.Entry#toJson()} method in your {@link Ingredient.Entry}.*/
    Ingredient.Entry fromJson(JsonObject object);

    static void init() {}

    /**
     * Try to deserialize a {@link Ingredient.Entry} from the given {@link JsonObject}.
     * @return the deserialized value, or null if not custom
     */
    @Nullable
    static Ingredient.Entry tryDeserializeJson(JsonObject object) {
        JsonElement type = object.get("value_deserializer");
        if (type != null && type.isJsonPrimitive()) {
            Identifier deserializerId = new Identifier(type.getAsString());
            ValueSerializer deserializer = ValueSerializer.REGISTRY.get(deserializerId);
            if (deserializer == null)
                throw new IllegalStateException("Value deserializer with ID not found: " + deserializerId);
            return deserializer.fromJson(object);
        }
        return null;
    }
}
