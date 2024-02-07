package net.george.blueprint.core.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.george.blueprint.core.Blueprint;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IngredientSerializer} handles turning packets and Json back into actual {@link Ingredient}s.
 */
@SuppressWarnings("unused")
public interface IngredientSerializer {
    /**
     * The {@link Registry} for deserializers.
     */
    Registry<IngredientSerializer> REGISTRY = FabricRegistryBuilder.createSimple(
            IngredientSerializer.class, new Identifier(Blueprint.MOD_ID, "ingredient_serializer")
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    /**
     * List of deserializer IDs that are referenced in recipes but were not found.
     * Used to avoid log spam.
     */
    List<Identifier> KNOWN_MISSING = new ArrayList<>();

    /**
     * Create an {@link Ingredient} from the packet.
     * This should reflect the corresponding {@link Ingredient#fromPacket(PacketByteBuf)} method in your {@link Ingredient}.
     */
    Ingredient fromNetwork(PacketByteBuf buffer);

    /**
     * Create an {@link Ingredient} from the given json object.
     * This should reflect the corresponding {@link Ingredient#toJson()} method in your {@link Ingredient}.
     */
    Ingredient fromJson(JsonObject object);

    /**
     * Try to deserialize an {@link Ingredient} from the given {@link JsonObject}.
     * @return the deserialized ingredient, or null if not custom
     */
    @Nullable
    static Ingredient tryDeserializeJson(JsonObject object) {
        JsonElement type = object.get("type");
        if (type != null && type.isJsonPrimitive()) {
            Identifier id = Identifier.tryParse(type.getAsString());
            if (id == null) {
                return null;
            }
            IngredientSerializer deserializer = IngredientSerializer.REGISTRY.get(id);
            if (deserializer != null) {
                try {
                    return deserializer.fromJson(object);
                } catch (JsonSyntaxException ex) {
                    Blueprint.LOGGER.error("Failed to deserialize Ingredient using deserializer [{}]: {}", id, ex.getMessage());
                }
            }
            if (KNOWN_MISSING.contains(id)) {
                return null;
            }
            KNOWN_MISSING.add(id);
            Blueprint.LOGGER.error("Ingredient Deserializer with ID not found: [{}] this can be ignored unless issues occur.", id);
        }
        return null;
    }

    /**
     * Try to deserialize an {@link Ingredient} from the given buffer.
     * @return the deserialized ingredient, or null if not custom
     */
    @Nullable
    static Ingredient tryDeserializeNetwork(PacketByteBuf buf) {
        int readIndex = buf.readerIndex();
        try {
            Identifier id = Identifier.tryParse(buf.readString());
            if (id != null && !id.getPath().isEmpty()) {
                IngredientSerializer deserializer = IngredientSerializer.REGISTRY.get(id);
                if (deserializer != null) {
                    return deserializer.fromNetwork(buf);
                }
                if (KNOWN_MISSING.contains(id)) {
                    return null;
                }
                KNOWN_MISSING.add(id);
                Blueprint.LOGGER.error("Ingredient Deserializer with ID not found: [{}] this can be ignored unless issues occur.", id);
            }
            buf.readerIndex(readIndex);
            return null;
        } catch (DecoderException exception) { // not a string
            buf.readerIndex(readIndex);
            return null;
        }
    }
}
