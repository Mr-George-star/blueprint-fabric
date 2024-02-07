package net.george.blueprint.common.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * A {@link TrackTargetGoal} extension that allows for conditioning the targeting of the owner.
 *
 * @author SmellyModder(Luke Tonon)
 */
public class PredicateAttackGoal<T extends LivingEntity> extends TrackTargetGoal {
    private final Predicate<MobEntity> canOwnerTarget;
    private final Class<T> targetClass;
    private final int targetChance;
    private LivingEntity nearestTarget;
    private final TargetPredicate targetEntitySelector;

    public PredicateAttackGoal(MobEntity goalOwner, Class<T> targetClass, boolean checkSight, Predicate<MobEntity> canOwnerTarget) {
        this(goalOwner, targetClass, checkSight, false, canOwnerTarget);
    }

    public PredicateAttackGoal(MobEntity goalOwner, Class<T> targetClass, boolean checkSight, boolean nearbyOnly, Predicate<MobEntity> canOwnerTarget) {
        this(goalOwner, targetClass, 10, checkSight, nearbyOnly, null, canOwnerTarget);
    }

    public PredicateAttackGoal(MobEntity goalOwner, Class<T> targetClass, int targetChance, boolean checkSight, boolean nearbyOnly, @Nullable Predicate<LivingEntity> targetPredicate, Predicate<MobEntity> canOwnerTarget) {
        super(goalOwner, checkSight, nearbyOnly);
        this.canOwnerTarget = canOwnerTarget;
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.targetEntitySelector = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
        this.setControls(EnumSet.of(Control.TARGET));
    }

    public boolean canStart() {
        if (!this.canOwnerTarget.test(this.mob) || (this.targetChance > 0 && this.mob.getRandom().nextInt(this.targetChance) != 0)) {
            return false;
        } else {
            this.findNearestTarget();
            return this.nearestTarget != null;
        }
    }

    protected Box getTargetableArea(double targetDistance) {
        return this.mob.getBoundingBox().expand(targetDistance, 4.0D, targetDistance);
    }

    protected void findNearestTarget() {
        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
            this.nearestTarget = this.mob.world.getClosestEntity(this.targetClass, this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetableArea(this.getFollowRange()));
        } else {
            this.nearestTarget = this.mob.world.getClosestPlayer(this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }
    }

    public void start() {
        this.mob.setTarget(this.nearestTarget);
        super.start();
    }
}
