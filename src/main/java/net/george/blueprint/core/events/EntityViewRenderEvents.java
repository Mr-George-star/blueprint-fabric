package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

public class EntityViewRenderEvents {
    public static final Event<CameraSetup> CAMERA_SETUP = EventFactory.createArrayBacked(CameraSetup.class,
            (listeners) -> (renderer, camera, tickDelta, yaw, pitch, roll) -> {
                for (CameraSetup callback : listeners) {
                    callback.interact(renderer, camera, tickDelta, yaw, pitch, roll);
                }
            });

    @FunctionalInterface
    public interface CameraSetup {
        void interact(GameRenderer renderer, Camera camera, double tickDelta, float yaw, float pitch, float roll);
    }
}
