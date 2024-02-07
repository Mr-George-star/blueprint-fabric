package net.george.blueprint.core.endimator.interpolation;

import com.mojang.serialization.Codec;
import net.george.blueprint.core.endimator.Endimation;
import net.george.blueprint.core.endimator.EndimationKeyframe;
import net.george.blueprint.core.endimator.Endimator;
import net.george.blueprint.core.util.registry.BasicRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

/**
 * The interface representing an identifiable function that applies progressed dimensional values from {@link EndimationKeyframe} instances.
 * Use {@link #REGISTRY} to register a new {@link InterpolationType}.
 *
 * @author SmellyModder (Luke Tonon)
 */
@FunctionalInterface
public interface InterpolationType {
    Registry REGISTRY = new Registry();

    InterpolationType LINEAR = REGISTRY.register(new Identifier("linear"), (vec3, keyframes, from, to, index, keyframeCount, progress) -> {
        vec3.set(MathHelper.lerp(progress, from.postX.get(), to.preX.get()), MathHelper.lerp(progress, from.postY.get(),
                to.preY.get()), MathHelper.lerp(progress, from.postZ.get(), to.preZ.get()));
    });

    InterpolationType CATMULL_ROM = REGISTRY.register(new Identifier("catmullrom"), (vec3, keyframes, from, to, index, keyframeCount, progress) -> {
        float fromX = from.postX.get();
        float fromY = from.postY.get();
        float fromZ = from.postZ.get();
        float oldFromX = fromX;
        float oldFromY = fromY;
        float oldFromZ = fromZ;
        int previousFromIndex = index - 2;
        if (previousFromIndex >= 0) {
            EndimationKeyframe oldFrom = keyframes[previousFromIndex];
            oldFromX = oldFrom.postX.get();
            oldFromY = oldFrom.postY.get();
            oldFromZ = oldFrom.postZ.get();
        }
        float toX = to.preX.get();
        float toY = to.preY.get();
        float toZ = to.preZ.get();
        float nextToX = toX;
        float nextToY = toY;
        float nextToZ = toZ;
        int nextToIndex = index + 1;
        if (nextToIndex < keyframeCount) {
            EndimationKeyframe nextTo = keyframes[nextToIndex];
            nextToX = nextTo.preX.get();
            nextToY = nextTo.preY.get();
            nextToZ = nextTo.preZ.get();
        }
        vec3.set(catmullRom(progress, oldFromX, fromX, toX, nextToX), catmullRom(progress, oldFromY, fromY, toY, nextToY), catmullRom(progress, oldFromZ, fromZ, toZ, nextToZ));
    });

    /**
     * Performs a Catmull-Rom interpolation for four points.
     *
     * @param progress The percentage of how far the curve is to being done.
     * @param p0       The first point.
     * @param p1       The second point.
     * @param p2       The third point.
     * @param p3       The fourth point.
     * @return The value of a Catmull-Rom interpolation for four points.
     * @deprecated This method is copied from the Mth class in 1.19 and will no longer be needed.
     */
    @Deprecated(forRemoval = true)
    static float catmullRom(float progress, float p0, float p1, float p2, float p3) {
        return 0.5F * (2.0F * p1 + (p2 - p0) * progress + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * progress * progress + (3.0F * p1 - p0 - 3.0F * p2 + p3) * progress * progress * progress);
    }

    /**
     * Handles how the interpolator applies the progressed dimensional values from {@link EndimationKeyframe} instances.
     *
     * @param vec3          A {@link Vec3f} instance to alter the values of.
     * @param keyframes     An array of {@link EndimationKeyframe} instances to use for relative frame reference.
     * @param from          The {@link EndimationKeyframe} instance being approached away from.
     * @param to         The {@link EndimationKeyframe} instance being approached to.
     * @param index         The index of the current {@link EndimationKeyframe}.
     * @param keyframeCount The length of the array of {@link EndimationKeyframe} instances.
     * @param progress      A percentage of how far the current {@link EndimationKeyframe} is to being done. Should be between 0 and 1.
     * @see Endimator#apply(Endimation, float, Endimator.ResetMode)
     * @see EndimationKeyframe#apply(Vec3f, EndimationKeyframe[], EndimationKeyframe, EndimationKeyframe, int, int, float)
     */
    void apply(Vec3f vec3, EndimationKeyframe[] keyframes, EndimationKeyframe from, EndimationKeyframe to, int index, int keyframeCount, float progress);

    /**
     * The class for wrapping the {@link InterpolationType} registry.
     * <p>This class exists to limit accessibility of the internal {@link #registry} and to allow the synchronized modifier.</p>
     *
     * @author SmellyModder (Luke Tonon)
     */
    final class Registry {
        private final BasicRegistry<InterpolationType> registry = new BasicRegistry<>();

        /**
         * Registers an {@link InterpolationType} with a {@link Identifier} name.
         *
         * @param name              A {@link Identifier} name for the type.
         * @param interpolationType An {@link InterpolationType} to register.
         * @return The registered {@link InterpolationType} instance.
         */
        public synchronized InterpolationType register(Identifier name, InterpolationType interpolationType) {
            this.registry.register(name, interpolationType);
            return interpolationType;
        }

        /**
         * Gets the {@link #registry} as a codec for serializing and deserializing {@link InterpolationType} types.
         *
         * @return The {@link #registry} as a codec.
         */
        public Codec<InterpolationType> asCodec() {
            return this.registry;
        }
    }
}
