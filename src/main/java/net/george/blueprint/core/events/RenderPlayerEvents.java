package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class RenderPlayerEvents {
    public static final Event<Post> POST = EventFactory.createArrayBacked(Post.class,
            (listeners) -> (player, g, matrixStack, vertexConsumerProvider, i) -> {
                for (Post callback : listeners) {
                    callback.interact(player, g, matrixStack, vertexConsumerProvider, i);
                }
            });

    @FunctionalInterface
    public interface Post {
        void interact(AbstractClientPlayerEntity player, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i);
    }
}
