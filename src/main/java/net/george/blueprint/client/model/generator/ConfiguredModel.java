package net.george.blueprint.client.model.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonObject;
import net.minecraft.client.render.model.ModelRotation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Represents a model with blockstate configurations, e.g. rotation, uvlock, and
 * random weight.
 * <p>
 * Can be manually constructed, created by static factory such as
 * {@link #allYRotations(ModelFile, int, boolean)}, or created by builder via
 * {@link #builder()}.
 */
@SuppressWarnings("unused")
public final class ConfiguredModel {
    /**
     * The default random weight of configured models, used by convenience
     * overloads.
     */
    public static final int DEFAULT_WEIGHT = 1;

    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    private static IntStream validRotations() {
        return IntStream.range(0, 4).map(i -> i * 90);
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
        return allYRotations(model, x, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(y -> new ConfiguredModel(model, x, y, uvlock, weight))
                .toArray(ConfiguredModel[]::new);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock) {
        return allRotations(model, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(x -> allYRotations(model, x, uvlock, weight))
                .flatMap(Arrays::stream)
                .toArray(ConfiguredModel[]::new);
    }

    /**
     * Construct a new {@link ConfiguredModel}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     * @param weight    the random weight of the model
     *
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link ModelRotation})
     * @throws IllegalArgumentException if weight is less than or equal to zero
     */
    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        Preconditions.checkNotNull(model);
        this.model = model;
        checkRotation(rotationX, rotationY);
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        checkWeight(weight);
        this.weight = weight;
    }

    /**
     * Construct a new {@link ConfiguredModel} with the {@link #DEFAULT_WEIGHT
     * default random weight}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     *
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link ModelRotation})
     */
    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock) {
        this(model, rotationX, rotationY, uvLock, DEFAULT_WEIGHT);
    }

    /**
     * Construct a new {@link ConfiguredModel} with the default rotation (0, 0),
     * uvlock (false), and {@link #DEFAULT_WEIGHT default random weight}.
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false);
    }

    static void checkRotation(int rotationX, int rotationY) {
        Preconditions.checkArgument(ModelRotation.get(rotationX, rotationY) != null, "Invalid model rotation x=%d, y=%d", rotationX, rotationY);
    }

    static void checkWeight(int weight) {
        Preconditions.checkArgument(weight >= 1, "Model weight must be greater than or equal to 1. Found: %d", weight);
    }

    JsonObject toJSON(boolean includeWeight) {
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("model", this.model.getId().toString());
        if (this.rotationX != 0)
            modelJson.addProperty("x", this.rotationX);
        if (this.rotationY != 0)
            modelJson.addProperty("y", this.rotationY);
        if (this.uvLock)
            modelJson.addProperty("uvlock", this.uvLock);
        if (includeWeight && this.weight != DEFAULT_WEIGHT)
            modelJson.addProperty("weight", this.weight);
        return modelJson;
    }

    /**
     * Create a new unowned {@link Builder}.
     *
     * @return the builder
     * @see Builder
     */
    public static Builder<?> builder() {
        return new Builder<>();
    }

    static Builder<VariantBlockStateBuilder> builder(VariantBlockStateBuilder outer, VariantBlockStateBuilder.PartialBlockstate state) {
        return new Builder<>(models -> outer.setModels(state, models), ImmutableList.of());
    }

    static Builder<MultiPartBlockStateBuilder.PartBuilder> builder(MultiPartBlockStateBuilder outer) {
        return new Builder<>(models -> {
            MultiPartBlockStateBuilder.PartBuilder result = outer.new PartBuilder(new BlockStateProvider.ConfiguredModelList(models));
            outer.addPart(result);
            return result;
        }, ImmutableList.of());
    }

    /**
     * A builder for {@link ConfiguredModel}s, which can contain a callback for
     * processing the finished result. If no callback is available (e.g. in the case
     * of {@link ConfiguredModel#builder()}), some methods will not be available.
     * <p>
     * Multiple models can be configured at once through the use of
     * {@link #nextModel()}.
     *
     * @param <T> the type of the owning builder, which supplied the callback, and
     *            will be returned upon completion.
     */
    public static class Builder<T> {

        private ModelFile model;
        @Nullable
        private final Function<ConfiguredModel[], T> callback;
        private final List<ConfiguredModel> otherModels;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = DEFAULT_WEIGHT;

        Builder() {
            this(null, ImmutableList.of());
        }

        Builder(@Nullable Function<ConfiguredModel[], T> callback, List<ConfiguredModel> otherModels) {
            this.callback = callback;
            this.otherModels = otherModels;
        }

        /**
         * Set the underlying model object for this configured model.
         *
         * @param model the model
         * @return this builder
         * @throws NullPointerException if {@code model} is {@code null}
         */
        public Builder<T> modelFile(ModelFile model) {
            Preconditions.checkNotNull(model, "Model must not be null");
            this.model = model;
            return this;
        }

        /**
         * Set the x-rotation for this model.
         *
         * @param value the x-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid x-rotation
         *                                  (see {@link ModelRotation})
         */
        public Builder<T> rotationX(int value) {
            checkRotation(value, this.rotationY);
            this.rotationX = value;
            return this;
        }

        /**
         * Set the y-rotation for this model.
         *
         * @param value the y-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid y-rotation
         *                                  (see {@link ModelRotation})
         */
        public Builder<T> rotationY(int value) {
            checkRotation(this.rotationX, value);
            this.rotationY = value;
            return this;
        }

        public Builder<T> uvLock(boolean value) {
            this.uvLock = value;
            return this;
        }

        /**
         * Set the random weight for this model.
         *
         * @param value the weight value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is less than or equal to
         *                                  zero
         */
        public Builder<T> weight(int value) {
            checkWeight(value);
            this.weight = value;
            return this;
        }

        /**
         * Build the most recent model, as if {@link #nextModel()} was never called.
         * Useful for single-model builders.
         *
         * @return the most recently configured model
         */
        public ConfiguredModel buildLast() {
            return new ConfiguredModel(this.model, this.rotationX, this.rotationY, this.uvLock, this.weight);
        }

        /**
         * Build all configured models and return them as an array.
         *
         * @return the array of built models.
         */
        public ConfiguredModel[] build() {
            return ObjectArrays.concat(this.otherModels.toArray(new ConfiguredModel[0]), buildLast());
        }

        /**
         * Apply the contained callback and return the owning builder object. What the
         * callback does is not defined by this class, but most likely it adds the built
         * models to the current variant being configured.
         * <p>
         * Known callbacks include:
         * <ul>
         * <li>{@link VariantBlockStateBuilder.PartialBlockstate#modelForState()}</li>
         * <li>{@link MultiPartBlockStateBuilder#part()}</li>
         * </ul>
         *
         * @return the owning builder object
         * @throws NullPointerException if there is no owning builder (and thus no callback)
         */
        public T addModel() {
            Preconditions.checkNotNull(this.callback, "Cannot use addModel() without an owning builder present");
            return this.callback.apply(build());
        }

        /**
         * Complete the current model and return a new builder instance with the same
         * callback, and storing all previously built models.
         *
         * @return a new builder for configuring the next model
         */
        public Builder<T> nextModel() {
            return new Builder<>(this.callback, Arrays.asList(build()));
        }
    }
}
