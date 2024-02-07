package net.george.blueprint.core.endimator;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.function.Function;

/**
 * An enum representing the possible transformations an {@link EndimatablePart} can receive from an {@link EndimationKeyframe}.
 *
 * @author SmellyModder (Luke Tonon)
 * @see EndimatablePart
 * @see Endimation.PartKeyframes
 * @see EndimationKeyframe
 */
public enum KeyframeType {
    POSITION(Endimation.PartKeyframes::getPosFrames, (pose, x, y, z, weight) -> pose.addPos(x * weight, y * weight, z * weight)),
    ROTATION(Endimation.PartKeyframes::getRotationFrames, (pose, x, y, z, weight) -> pose.addRotation(x * weight * MathHelper.RADIANS_PER_DEGREE, y * weight * MathHelper.RADIANS_PER_DEGREE,
            z * weight * MathHelper.RADIANS_PER_DEGREE)),
    OFFSET(Endimation.PartKeyframes::getOffsetFrames, (pose, x, y, z, weight) -> pose.addOffset(x * weight, y * weight, z * weight)),
    SCALE(Endimation.PartKeyframes::getScaleFrames, (pose, x, y, z, weight) -> pose.addScale(weight * (x - 1.0F), weight * (y - 1.0F), weight * (z - 1.0F)));

    private final Function<Endimation.PartKeyframes, EndimationKeyframe[]> getter;
    private final Procedure procedure;

    KeyframeType(Function<Endimation.PartKeyframes, EndimationKeyframe[]> getter, Procedure procedure) {
        this.getter = getter;
        this.procedure = procedure;
    }

    /**
     * Gets the array of {@link EndimationKeyframe} instances associated with this type for a given {@link Endimation.PartKeyframes} instance.
     *
     * @param partKeyframes A {@link Endimation.PartKeyframes} instance to get the array of {@link EndimationKeyframe} instances from.
     * @return The array of {@link EndimationKeyframe} instances associated with this type for a given {@link Endimation.PartKeyframes} instance.
     */
    public EndimationKeyframe[] getFrames(Endimation.PartKeyframes partKeyframes) {
        return this.getter.apply(partKeyframes);
    }

    /**
     * Applies a given transformation {@link Vec3f} to a {@link Endimator.PosedPart} using this type's {@link #procedure}.
     *
     * @param pose     A {@link Endimator.PosedPart} to apply to.
     * @param vector3f A transformation {@link Vec3f} to apply.
     * @param weight   The weight multiplier.
     * @see Endimator#apply(Endimation, float, Endimator.ResetMode)
     */
    public void apply(Endimator.PosedPart pose, Vec3f vector3f, float weight) {
        this.procedure.apply(pose, vector3f.getX(), vector3f.getY(), vector3f.getZ(), weight);
    }

    @FunctionalInterface
    interface Procedure {
        void apply(Endimator.PosedPart pose, float x, float y, float z, float weight);
    }
}
