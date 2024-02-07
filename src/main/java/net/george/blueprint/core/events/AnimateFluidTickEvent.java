package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.george.blueprint.core.mixin.client.ClientWorldMixin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * This event is fired before {@link FluidState#randomDisplayTick(World, BlockPos, Random)} gets called in {@link ClientWorld}.
 * <p>Canceling this event will prevent the original method from being called.</p>
 *
 * @author abigailfails
 * @see ClientWorldMixin
 */
public interface AnimateFluidTickEvent {
    Event<AnimateFluidTickEvent> EVENT = EventFactory.createArrayBacked(AnimateFluidTickEvent.class,
            (listeners) -> (world, pos, state, random) -> {
                for (AnimateFluidTickEvent callback : listeners) {
                    boolean result = callback.interact(world, pos, state, random);

                    if (result) {
                        return true;
                    }
                }
                return false;
            });

    boolean interact(World world, BlockPos pos, FluidState state, Random random);

    /**
     * Fires the {@link AnimateFluidTickEvent} for a given {@link FluidState}, {@link World}, {@link BlockPos} and {@link Random}.
     *
     * @param world  The {@link World} that the {@code state} is in.
     * @param pos    The {@link BlockPos} that the {@code state} is at.
     * @param state  The {@link FluidState} that {@link FluidState#randomDisplayTick(World, BlockPos, Random)} is being fired for.
     * @param random The {@link Random} to be used for randomizing particle placement.
     */
    static boolean onAnimateFluidTick(World world, BlockPos pos, FluidState state, Random random) {
        return EVENT.invoker().interact(world, pos, state, random);
    }
}
