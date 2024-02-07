package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.ClientConnection;

public interface PlayerLoginCallback {
    Event<PlayerLoginCallback> EVENT = EventFactory.createArrayBacked(PlayerLoginCallback.class,
            (listeners) -> (interactionManager, clientPlayer, connection) -> {
                for (PlayerLoginCallback callback : listeners) {
                    callback.interact(interactionManager, clientPlayer, connection);
                }
            });

    void interact(ClientPlayerInteractionManager interactionManager, ClientPlayerEntity clientPlayer, ClientConnection connection);
}
