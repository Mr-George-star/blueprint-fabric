package net.george.blueprint.common.item;

import net.george.blueprint.common.entity.BlueprintBoat;
import net.george.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

/**
 * An {@link Item} extension used for Blueprint's boats.
 * <p>This {@link Item} will also fill itself after the latest vanilla boat item.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public class BlueprintBoatItem extends Item {
    private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.DARK_OAK_BOAT);
    private static final Predicate<Entity> COLLISION_PREDICATE = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::collides);
    private final String type;

    public BlueprintBoatItem(String type, Settings settings) {
        super(settings);
        this.type = type;
        DispenserBlock.registerBehavior(this, new DispenserBoatBehavior(type));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        FILLER.fillItem(this, group, stacks);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        HitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return new TypedActionResult<>(ActionResult.PASS, itemstack);
        } else {
            Vec3d vec3d = player.getRotationVec(1.0F);
            List<Entity> list = world.getOtherEntities(player, player.getBoundingBox().stretch(vec3d.multiply(5.0D)).expand(1.0D), COLLISION_PREDICATE);
            if (!list.isEmpty()) {
                Vec3d vec3d1 = player.getCameraPosVec(1.0F);

                for (Entity entity : list) {
                    Box aabb = entity.getBoundingBox().expand(entity.getTargetingMargin());
                    if (aabb.contains(vec3d1)) {
                        return new TypedActionResult<>(ActionResult.PASS, itemstack);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlueprintBoat boat = new BlueprintBoat(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                boat.setBoat(this.type);
                boat.setYaw(player.getYaw());
                if (!world.isSpaceEmpty(boat, boat.getBoundingBox().expand(-0.1D))) {
                    return new TypedActionResult<>(ActionResult.FAIL, itemstack);
                } else {
                    if (!world.isClient) {
                        world.spawnEntity(boat);
                    }

                    if (!player.getAbilities().creativeMode) {
                        itemstack.decrement(1);
                    }

                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
                }
            } else {
                return new TypedActionResult<>(ActionResult.PASS, itemstack);
            }
        }
    }

    static class DispenserBoatBehavior extends ItemDispenserBehavior {
        private final ItemDispenserBehavior DEFAULT_BEHAVIOR = new ItemDispenserBehavior();
        private final String type;

        public DispenserBoatBehavior(String type) {
            this.type = type;
        }

        public ItemStack execute(BlockPointer source, ItemStack stack) {
            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            World level = source.getWorld() ;
            double x = source.getX() + (double) ((float) direction.getOffsetX() * 1.125f);
            double y = source.getY() + (double) ((float) direction.getOffsetY() * 1.125f);
            double z = source.getZ() + (double) ((float) direction.getOffsetZ() * 1.125f);
            BlockPos pos = source.getPos().offset(direction);
            double adjustY;
            if (level.getFluidState(pos).isIn(FluidTags.WATER)) {
                adjustY = 1d;
            } else {
                if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos.down()).isIn(FluidTags.WATER)) {
                    return DEFAULT_BEHAVIOR.dispense(source, stack);
                }
                adjustY = 0d;
            }
            BlueprintBoat boat = new BlueprintBoat(level, x, y + adjustY, z);
            boat.setBoat(this.type);
            boat.setYaw(direction.asRotation());
            level.spawnEntity(boat);
            stack.decrement(1);
            return stack;
        }

        protected void playSound(BlockPointer source) {
            source.getWorld().syncWorldEvent(1000, source.getPos(), 0);
        }
    }
}
