package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.util.Identifier;

/**
 * An event fired for when an {@link Advancement} is being deserialized and built.
 *
 * @author SmellyModder (Luke Tonon)
 * @author Mr.George
 */
public interface AdvancementBuildingEvent {
    Event<AdvancementBuildingEvent> EVENT = EventFactory.createArrayBacked(AdvancementBuildingEvent.class,
            (listeners) -> (builder, id) -> {
                for (AdvancementBuildingEvent callback : listeners) {
                    callback.interact(builder, id);
                }
            });

    void interact(Advancement.Builder builder, Identifier id);

    /**
     * Fires the {@link AdvancementBuildingEvent} for a given {@link Advancement.Builder} and {@link Identifier} advancement name.
     *
     * @param builder  The {@link Advancement.Builder} being built.
     * @param id The {@link Identifier} of the {@link Advancement} being built.
     */
    static void onBuildingAdvancement(Advancement.Builder builder, Identifier id) {
        EVENT.invoker().interact(builder, id);
    }
}
