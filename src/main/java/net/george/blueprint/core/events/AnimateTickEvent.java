package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * This event is fired before {@link Block#randomDisplayTick(BlockState, World, BlockPos, Random)}.
 * <p>Canceling this event will prevent the original method from being called.</p>
 *
 * @author abigailfails
 */
public interface AnimateTickEvent {
    Event<AnimateTickEvent> EVENT = EventFactory.createArrayBacked(AnimateTickEvent.class,
            (listeners) -> (state, world, pos, random) -> {
                for (AnimateTickEvent callback : listeners) {
                    boolean result = callback.interact(state, world, pos, random);

                    if (result) {
                        return true;
                    }
                }
                return false;
            });

    boolean interact(BlockState state, World world, BlockPos pos, Random random);

    /**
     * Fires the {@link AnimateTickEvent} for a given {@link BlockState}, {@link World}, {@link BlockPos} and {@link Random}.
     *
     * @param state  The {@link BlockState} that {@link Block#randomDisplayTick(BlockState, World, BlockPos, Random)} is being fired for.
     * @param world  The {@link World} that the {@code state} is in.
     * @param pos    The {@link BlockPos} that the {@code state} is at.
     * @param random The {@link Random} to be used for randomizing particle placement.
     */
    static boolean onAnimateTick(BlockState state, World world, BlockPos pos, Random random) {
        return EVENT.invoker().interact(state, world, pos, random);
    }
}
