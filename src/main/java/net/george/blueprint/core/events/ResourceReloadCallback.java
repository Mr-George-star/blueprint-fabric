package net.george.blueprint.core.events;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.DataPackContents;

import java.util.List;

public interface ResourceReloadCallback {
    Event<ResourceReloadCallback> EVENT = EventFactory.createArrayBacked(ResourceReloadCallback.class,
            (listeners) -> (contents, reloaders) -> {
                for (ResourceReloadCallback callback : listeners) {
                    List<ResourceReloader> result = callback.interact(contents, reloaders);

                    if (!result.isEmpty()) {
                        return result;
                    }
                }
                return Lists.newArrayList();
            });

    List<ResourceReloader> interact(DataPackContents contents, List<ResourceReloader> reloaders);

}
