package net.george.blueprint.core.endimator.interpolation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.george.blueprint.core.endimator.EndimationKeyframe;
import net.minecraft.util.math.Vec3f;

import java.util.function.Function;

/**
 * The record class for applying an {@link InterpolationType} instance and an easing function.
 * <p>The internal {@link #easer} transforms the keyframe progress and the internal {@link #type} gets applied to the {@link Vec3f} instance.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record EndimationInterpolator(InterpolationType type, Function<Float, Float> easer) {
    public static final Codec<EndimationInterpolator> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(InterpolationType.REGISTRY.asCodec().fieldOf("type").forGetter((interpolator) ->
                    interpolator.type), EndimationEasers.REGISTRY.optionalFieldOf("easing", EndimationEasers.LINEAR)
                    .forGetter((interpolator) -> interpolator.easer)).apply(instance, EndimationInterpolator::new));

    public EndimationInterpolator(InterpolationType type, Function<Float, Float> easer) {
        this.type = type;
        this.easer = easer;
    }

    /**
     * Applies the internal {@link #type} to the given {@link Vec3f} instance, using a progress value transformed by the internal {@link #easer}.
     *
     * @param vec3          A {@link Vec3f} instance to apply the interpolated keyframe values.
     * @param keyframes     An array of {@link EndimationKeyframe} instances to use for relative frame reference.
     * @param from          The {@link EndimationKeyframe} instance getting receded from.
     * @param to            The {@link EndimationKeyframe} instance getting approached to.
     * @param index         The index of the current {@link EndimationKeyframe} instance.
     * @param keyframeCount The length of the array of {@link EndimationKeyframe} instances.
     * @param progress      A percentage of how far the current {@link EndimationKeyframe} is to being done. Should be between 0 and 1.
     */
    public void apply(Vec3f vec3, EndimationKeyframe[] keyframes, EndimationKeyframe from, EndimationKeyframe to, int index, int keyframeCount, float progress) {
        this.type.apply(vec3, keyframes, from, to, index, keyframeCount, this.easer.apply(progress));
    }

    public InterpolationType type() {
        return this.type;
    }

    public Function<Float, Float> easer() {
        return this.easer;
    }
}
