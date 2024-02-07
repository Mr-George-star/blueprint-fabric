package net.george.blueprint.core.events;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;

import java.util.List;
import java.util.function.Consumer;

public interface RegisteredShadersCallback {
    Event<RegisteredShadersCallback> EVENT = EventFactory.createArrayBacked(RegisteredShadersCallback.class,
            (listeners) -> (manager, list) -> {
                for (RegisteredShadersCallback callback : listeners) {
                    callback.interact(manager, list);
                }
            });

    void interact(ResourceManager manager, List<Pair<Shader, Consumer<Shader>>> list);
}
