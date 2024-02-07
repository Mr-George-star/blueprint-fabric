package net.george.blueprint.core.api;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * This class contains some useful methods for generating random positions.
 *
 * @author SmellyModder(Luke Tonon)
 */
@SuppressWarnings("unused")
public final class AdvancedRandomPos {
    /**
     * Finds a random target within xz and y
     */
    @Nullable
    public static Vec3d findRandomTarget(PathAwareEntity pathfinder, int xz, int y, boolean goDeep) {
        return findRandomTargetBlock(pathfinder, xz, y, null, goDeep);
    }

    @Nullable
    private static Vec3d findRandomTargetBlock(PathAwareEntity entity, int xz, int y, @Nullable Vec3d targetVec, boolean goDeep) {
        return generateRandomPos(entity, xz, y, targetVec, true, Math.PI / 2F, goDeep, entity::getPathfindingFavor);
    }

    @Nullable
    private static Vec3d generateRandomPos(PathAwareEntity entity, int xz, int y, @Nullable Vec3d vec3d, boolean p_191379_4_, double angle, boolean goDeep, ToDoubleFunction<BlockPos> p_191379_7_) {
        EntityNavigation navigation = entity.getNavigation();
        Random random = entity.getRandom();
        boolean flag = entity.hasPositionTarget() && entity.getPositionTarget().isWithinDistance(entity.getBlockPos(), (double) (entity.getPositionTargetRange() + (float) xz) + 1.0D);
        boolean flag1 = false;
        double d0 = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = new BlockPos(entity.getPos());

        for (int i = 0; i < 10; ++i) {
            BlockPos blockPos1 = getBlockPos(random, xz, y, vec3d, angle, goDeep);
            if (blockPos1 != null) {
                int j = blockPos1.getX();
                int k = blockPos1.getY();
                int l = blockPos1.getZ();
                if (entity.hasPositionTarget() && xz > 1) {
                    BlockPos blockPos2 = entity.getPositionTarget();
                    if (entity.getX() > (double) blockPos2.getX()) {
                        j -= random.nextInt(xz / 2);
                    } else {
                        j += random.nextInt(xz / 2);
                    }

                    if (entity.getZ() > (double) blockPos2.getZ()) {
                        l -= random.nextInt(xz / 2);
                    } else {
                        l += random.nextInt(xz / 2);
                    }
                }

                BlockPos blockPos3 = new BlockPos((double) j + entity.getX(), (double) k + entity.getY(), (double) l + entity.getZ());
                if ((!flag || entity.isInWalkTargetRange(blockPos3)) && navigation.isValidPosition(blockPos3)) {
                    if (!p_191379_4_) {
                        blockPos3 = moveAboveSolid(blockPos3, entity);
                        if (isWaterDestination(blockPos3, entity)) {
                            continue;
                        }
                    }

                    double d1 = p_191379_7_.applyAsDouble(blockPos3);
                    if (d1 > d0) {
                        d0 = d1;
                        blockPos = blockPos3;
                        flag1 = true;
                    }
                }
            }
        }

        if (flag1) {
            return Vec3d.ofCenter(blockPos);
        } else {
            return null;
        }
    }

    @Nullable
    private static BlockPos getBlockPos(Random random, int xz, int y, @Nullable Vec3d vector3d, double angle, boolean goDeep) {
        if (vector3d != null && !(angle >= Math.PI)) {
            double d3 = MathHelper.atan2(vector3d.z, vector3d.x) - (double) ((float) Math.PI / 2F);
            double d4 = d3 + (double) (2.0F * random.nextFloat() - 1.0F) * angle;
            double d0 = Math.sqrt(random.nextDouble()) * (double) MathHelper.SQUARE_ROOT_OF_TWO * (double) xz;
            double d1 = -d0 * Math.sin(d4);
            double d2 = d0 * Math.cos(d4);
            if (!(Math.abs(d1) > (double) xz) && !(Math.abs(d2) > (double) xz)) {
                double newY = random.nextInt(2 * y + 1) - y;
                return new BlockPos(d1, newY, d2);
            } else {
                return null;
            }
        } else {
            int newX = random.nextInt(2 * xz + 1) - xz;
            int newY = random.nextInt(2 * y + 1) - y;
            int newZ = random.nextInt(2 * xz + 1) - xz;
            if (goDeep) {
                newY = random.nextInt(y + 1) - y * 2;
            }
            return new BlockPos(newX, newY, newZ);
        }
    }

    private static BlockPos moveAboveSolid(BlockPos pos, PathAwareEntity entity) {
        if (!entity.world.getBlockState(pos).getMaterial().isSolid()) {
            return pos;
        } else {
            BlockPos blockPos;
            for (blockPos = pos.up(); blockPos.getY() < entity.world.getTopY() && entity.world.getBlockState(blockPos).getMaterial().isSolid(); blockPos = blockPos.up()  ) {
            }

            return blockPos;
        }
    }

    private static boolean isWaterDestination(BlockPos pos, PathAwareEntity entity) {
        return entity.world.getFluidState(pos).isIn(FluidTags.WATER);
    }
}
