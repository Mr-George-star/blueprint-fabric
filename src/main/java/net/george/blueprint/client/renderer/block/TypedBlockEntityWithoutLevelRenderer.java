package net.george.blueprint.client.renderer.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * A utility {@link BuiltinModelItemRenderer} extension for simple rendering of {@link BlockEntity} items.
 *
 * @param <E> The type of {@link BlockEntity} the renderer is for.
 */
@Environment(EnvType.CLIENT)
public class TypedBlockEntityWithoutLevelRenderer<E extends BlockEntity> extends BuiltinModelItemRenderer {
    private final E entity;

    public TypedBlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelLoader loader, E entity) {
        super(dispatcher, loader);
        this.entity = entity;
    }

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.blockEntityRenderDispatcher.renderEntity(this.entity, matrices, vertexConsumers, light, overlay);
    }
}
