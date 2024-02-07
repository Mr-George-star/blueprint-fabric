package net.george.blueprint.common.world.modification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.util.BiomeUtil;
import net.george.blueprint.core.util.modification.selection.ConditionedResourceSelector;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

/**
 * The record class for representing a weighted slice of the world that uses a {@link BiomeUtil.ModdedBiomeProvider} instance for selecting biomes.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record ModdedBiomeSlice(Identifier name, int weight, BiomeUtil.ModdedBiomeProvider provider) {
    public static final Codec<ModdedBiomeSlice> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(Identifier.CODEC.fieldOf("name").forGetter((slice) -> slice.name),
            Codecs.NONNEGATIVE_INT.fieldOf("weight").forGetter((slice) -> slice.weight),
            BiomeUtil.ModdedBiomeProvider.CODEC.fieldOf("provider").forGetter((slice) -> slice.provider))
            .apply(instance, ModdedBiomeSlice::new));
    private static final Pair<ConditionedResourceSelector, ModdedBiomeSlice> SKIPPED;

    public ModdedBiomeSlice(Identifier name, int weight, BiomeUtil.ModdedBiomeProvider provider) {
        this.name = name;
        this.weight = weight;
        this.provider = provider;
    }

    /**
     * Deserializes a {@link JsonElement} instance into a pair containing a {@link ConditionedResourceSelector} instance and a {@link ModdedBiomeSlice} instance.
     *
     * @param name    The name of the slice getting deserialized.
     * @param element A {@link JsonElement} instance to deserialize.
     * @param ops     A {@link DynamicOps} instance to use for decoding {@link BiomeUtil.ModdedBiomeProvider} instances.
     * @return A pair containing a new {@link ConditionedResourceSelector} instance and a new {@link ModdedBiomeSlice} instance from a {@link JsonElement} instance.
     * @throws JsonParseException If a problem occurs when deserializing.
     */
    public static Pair<ConditionedResourceSelector, ModdedBiomeSlice> deserializeWithSelector(Identifier name, JsonElement element, DynamicOps<JsonElement> ops) throws JsonParseException {
        JsonObject object = JsonHelper.asObject(element, element.toString());
        ConditionedResourceSelector selector = ConditionedResourceSelector.deserialize("selector", object.get("selector"));
        if (selector == ConditionedResourceSelector.EMPTY) {
            Blueprint.LOGGER.info("Skipped modded biome slice named '" + name + "' as its conditions were not met");
            return SKIPPED;
        } else {
            DataResult<Pair<ModdedBiomeSlice, JsonElement>> sliceResult = CODEC.decode(ops, object);
            Optional<DataResult.PartialResult<Pair<ModdedBiomeSlice, JsonElement>>> sliceError = sliceResult.error();
            if (sliceError.isPresent()) {
                throw new JsonParseException(sliceError.get().message());
            } else {
                return Pair.of(selector, sliceResult.result().get().getFirst());
            }
        }
    }

    /**
     * Serializes a {@link ConditionedResourceSelector} instance and this {@link ModdedBiomeSlice} instance into a {@link JsonObject} instance.
     *
     * @param selector A {@link ConditionedResourceSelector} instance to serialize with this slice.
     * @param ops      A {@link DynamicOps} instance to use for encoding the {@link #provider}.
     * @return A {@link JsonObject} instance representing a {@link ModdedBiomeSlice} instance with a selector.
     * @throws JsonParseException If a problem occurs when serializing.
     */
    public JsonElement serializeWithSelector(ConditionedResourceSelector selector, DynamicOps<JsonElement> ops) throws JsonParseException {
        JsonObject object = new JsonObject();
        object.add("selector", selector.serialize());
        DataResult<JsonElement> result = CODEC.encode(this, ops, object);
        Optional<DataResult.PartialResult<JsonElement>> error = result.error();
        if (error.isPresent()) {
            throw new JsonParseException(error.get().message());
        } else {
            return result.get().left().get();
        }
    }

    public Identifier name() {
        return this.name;
    }

    public int weight() {
        return this.weight;
    }

    public BiomeUtil.ModdedBiomeProvider provider() {
        return this.provider;
    }

    static {
        SKIPPED = Pair.of(ConditionedResourceSelector.EMPTY, new ModdedBiomeSlice(new Identifier("blueprint", "skipped"), 0, new BiomeUtil.OriginalModdedBiomeProvider()));
    }
}
