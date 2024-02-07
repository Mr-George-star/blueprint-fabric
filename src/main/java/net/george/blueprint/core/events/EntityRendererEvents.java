package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;

public class EntityRendererEvents {
    public static final Event<AddLayers> ADD_LAYERS = EventFactory.createArrayBacked(AddLayers.class,
            (listeners) -> (renderers, playerRenderers) -> {
                for (AddLayers callback : listeners) {
                    callback.interact(renderers, playerRenderers);
                }
            });

    @FunctionalInterface
    public interface AddLayers {
        void interact(Map<EntityType<?>, EntityRenderer<?>> renderers, Map<String, EntityRenderer<? extends PlayerEntity>> playerRenderers);
    }
}
