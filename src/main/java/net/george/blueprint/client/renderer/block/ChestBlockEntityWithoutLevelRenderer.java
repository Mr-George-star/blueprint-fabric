package net.george.blueprint.client.renderer.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * The {@link TypedBlockEntityWithoutLevelRenderer} responsible for rendering the items of Blueprint's chests.
 *
 * @param <C> The type of {@link BlockEntity} the renderer is for.
 */
@Environment(EnvType.CLIENT)
public class ChestBlockEntityWithoutLevelRenderer<C extends BlockEntity> extends TypedBlockEntityWithoutLevelRenderer<C> {
    public ChestBlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelLoader loader, C entity) {
        super(dispatcher, loader, entity);
    }

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockItem blockItem = (BlockItem) stack.getItem();
        BlueprintChestBlockEntityRenderer.itemBlock = blockItem.getBlock();
        super.render(stack, mode, matrices, vertexConsumers, light, overlay);
        BlueprintChestBlockEntityRenderer.itemBlock = null;
    }

}
