package net.george.blueprint.core.events;

import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface TextureStitchedCallback {
    Event<TextureStitchedCallback> EVENT = EventFactory.createArrayBacked(TextureStitchedCallback.class,
            (listeners) -> (texture, set) -> {
                Set<Identifier> called = Sets.newHashSet();
                for (TextureStitchedCallback callback : listeners) {
                    Set<Identifier> result = callback.interact(texture, set);
                    called.addAll(result);
                }
                return called;
            });

    Set<Identifier> interact(SpriteAtlasTexture texture, Set<Identifier> set);
}
