package net.george.blueprint.core.events;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * An event that gets fired when a {@link JsonDataLoader} instance finishes preparing its entries.
 *
 * @author SmellyModder (Luke Tonon)
 * @author Mr.George
 * @see JsonDataLoader
 */
public interface SimpleJsonResourceListenerPreparedEvent {
    Event<SimpleJsonResourceListenerPreparedEvent> EVENT = EventFactory.createArrayBacked(SimpleJsonResourceListenerPreparedEvent.class,
            (listeners) -> (gson, directory, entries) -> {
                for (SimpleJsonResourceListenerPreparedEvent callback : listeners) {
                    callback.interact(gson, directory, entries);
                }
            });

    void interact(Gson gson, String directory, Map<Identifier, JsonElement> entries);
}
