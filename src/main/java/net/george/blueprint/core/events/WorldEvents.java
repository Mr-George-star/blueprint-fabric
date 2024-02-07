package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.WorldAccess;

public class WorldEvents {
    public static final Event<Unload> UNLOAD = EventFactory.createArrayBacked(Unload.class,
            (listeners) -> world -> {
                for (Unload callback : listeners) {
                    callback.interact(world);
                }
            });

    @FunctionalInterface
    public interface Unload {
        void interact(WorldAccess world);
    }
}
