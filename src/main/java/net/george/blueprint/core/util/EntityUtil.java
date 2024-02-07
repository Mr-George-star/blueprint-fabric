package net.george.blueprint.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class containing some useful entity methods.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class EntityUtil {
    /**
     * Traces a given {@link Entity} for a given distance and delta.
     *
     * @param entity   An {@link Entity} to trace from.
     * @param distance The distance of the trace.
     * @param delta    The delta of the trace.
     * @return The {@link HitResult} of the traced entity.
     */
    public static HitResult rayTrace(Entity entity, double distance, float delta) {
        return entity.world.raycast(new RaycastContext(
                entity.getCameraPosVec(delta),
                entity.getCameraPosVec(delta).add(entity.getRotationVec(delta).multiply(distance)),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));
    }

    /**
     * Traces a given {@link Entity} at a custom direction for a given distance and delta.
     *
     * @param entity   An {@link Entity} to trace from.
     * @param pitch    The pitch for the trace.
     * @param yaw      The yaw for the trace.
     * @param distance The distance of the trace.
     * @param delta    The delta of the trace.
     * @return The {@link HitResult} of the traced entity.
     */
    public static HitResult rayTraceWithCustomDirection(Entity entity, float pitch, float yaw, double distance, float delta) {
        return entity.world.raycast(new RaycastContext(
                entity.getCameraPosVec(delta),
                entity.getCameraPosVec(delta).add(getVectorForRotation(pitch, yaw).multiply(distance)),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));
    }

    /**
     * Upwardly traces a given {@link Entity} at a custom direction for a given distance and delta.
     *
     * @param entity   An {@link Entity} to trace from.
     * @param pitch    The pitch for the trace.
     * @param yaw      The yaw for the trace.
     * @param distance The distance of the trace.
     * @param delta    The delta of the trace.
     * @return The {@link HitResult} of the traced entity.
     */
    public static HitResult rayTraceUpWithCustomDirection(Entity entity, float pitch, float yaw, double distance, float delta) {
        return entity.world.raycast(new RaycastContext(
                entity.getCameraPosVec(delta),
                entity.getCameraPosVec(delta).add(getUpVectorForRotation(pitch, yaw).multiply(distance)),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));
    }

    /**
     * Gets the {@link Vec3d} for a yaw and a pitch.
     *
     * @param pitch The pitch to use.
     * @param yaw   The yaw to use.
     * @return The {@link Vec3d} for a yaw and a pitch.
     */
    public static Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float) Math.PI / 180F);
        float f1 = -yaw * ((float) Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vec3d(f3 * f4, -f5, f2 * f4);
    }

    /**
     * Gets the upward {@link Vec3d} for a yaw and a pitch.
     *
     * @param pitch The pitch to use.
     * @param yaw   The yaw to use.
     * @return The upward {@link Vec3d} for a yaw and a pitch.
     */
    public static Vec3d getUpVectorForRotation(float pitch, float yaw) {
        float f = (pitch - 90.0F) * ((float) Math.PI / 180F);
        float f1 = -yaw * ((float) Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vec3d(f3 * f4, -f5, f2 * f4);
    }
}
