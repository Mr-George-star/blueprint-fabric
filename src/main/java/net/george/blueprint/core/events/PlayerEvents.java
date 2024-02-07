package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class PlayerEvents {
    public static final Event<Clone> CLONE = EventFactory.createArrayBacked(Clone.class,
            (listeners) -> (player, oldPlayer, alive) -> {
                for (Clone callback : listeners) {
                    callback.interact(player, oldPlayer, alive);
                }
            });

    public static final Event<ChangeDimension> CHANGE_DIMENSION = EventFactory.createArrayBacked(ChangeDimension.class,
            (listeners) -> (player, fromDimension, toDimension) -> {
                for (ChangeDimension callback : listeners) {
                    callback.interact(player, fromDimension, toDimension);
                }
            });

    @FunctionalInterface
    public interface Clone {
        void interact(ServerPlayerEntity player, ServerPlayerEntity oldPlayer, boolean alive);
    }

    @FunctionalInterface
    public interface ChangeDimension {
        void interact(ServerPlayerEntity player, RegistryKey<World> fromDimension, RegistryKey<World> toDimension);
    }
}
