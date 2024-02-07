package net.george.blueprint.core.api.recipe.condition;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused")
public interface IConditionSerializer<T extends ICondition> {
    /**
     * The {@link Registry} for deserializers.
     */
    @SuppressWarnings("rawtypes")
    Registry<IConditionSerializer> REGISTRY = FabricRegistryBuilder.createSimple(
            IConditionSerializer.class, new Identifier(Blueprint.MOD_ID, "condition_serializer")
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    void write(JsonObject json, T value);

    T read(JsonObject json);

    Identifier getId();

    default JsonObject getJson(T value) {
        JsonObject json = new JsonObject();
        this.write(json, value);
        json.addProperty("type", value.getId().toString());
        return json;
    }
}
