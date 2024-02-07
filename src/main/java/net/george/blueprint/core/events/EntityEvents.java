package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityEvents {
    public static final Event<JoinWorld> JOIN_WORLD = EventFactory.createArrayBacked(JoinWorld.class,
            (listeners) -> (entity, world, exciting) -> {
                for (JoinWorld callback : listeners) {
                    callback.interact(entity, world, exciting);
                }
            });

    @FunctionalInterface
    public interface JoinWorld {
        void interact(Entity entity, World world, boolean exciting);
    }
}
