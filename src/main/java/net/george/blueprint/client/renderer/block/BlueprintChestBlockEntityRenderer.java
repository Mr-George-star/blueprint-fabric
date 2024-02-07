package net.george.blueprint.client.renderer.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.client.ChestManager;
import net.george.blueprint.core.api.IChestBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Calendar;

/**
 * The {@link BlockEntityRenderer} responsible for the rendering of Blueprint's chests.
 *
 * @param <T> The type of {@link ChestAnimationProgress} the renderer is for.
 */
@Environment(EnvType.CLIENT)
public class BlueprintChestBlockEntityRenderer<T extends BlockEntity & ChestAnimationProgress> implements BlockEntityRenderer<T> {
    public static Block itemBlock = null;

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    public boolean isChristmas;

    public BlueprintChestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26) {
            this.isChristmas = true;
        }
        ModelPart modelPart = context.getLayerModelPart(EntityModelLayers.CHEST);
        this.bottom = modelPart.getChild("bottom");
        this.lid = modelPart.getChild("lid");
        this.lock = modelPart.getChild("lock");
        ModelPart modelPart1 = context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = modelPart1.getChild("bottom");
        this.doubleLeftLid = modelPart1.getChild("lid");
        this.doubleLeftLock = modelPart1.getChild("lock");
        ModelPart modelPart2 = context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = modelPart2.getChild("bottom");
        this.doubleRightLid = modelPart2.getChild("lid");
        this.doubleRightLock = modelPart1.getChild("lock");
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        boolean flag = world != null;
        BlockState blockState = flag ? entity.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType = blockState.contains(ChestBlock.CHEST_TYPE) ? blockState.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
        Block block = blockState.getBlock();
        if (block instanceof AbstractChestBlock<?> abstractChestBlock) {
            boolean flag1 = chestType != ChestType.SINGLE;
            matrices.push();
            float rotation = blockState.get(ChestBlock.FACING).asRotation();
            matrices.translate(0.5D, 0.5D, 0.5D);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-rotation));
            matrices.translate(-0.5D, -0.5D, -0.5D);
            DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> propertySource;
            if (flag) {
                propertySource = abstractChestBlock.getBlockEntitySource(blockState, world, entity.getPos(), true);
            } else {
                propertySource = DoubleBlockProperties.PropertyRetriever::getFallback;
            }

            float f1 = propertySource.apply(ChestBlock.getAnimationProgressRetriever(entity)).get(tickDelta);
            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            int i = propertySource.apply(new LightmapCoordinatesRetriever<>()).applyAsInt(light);
            VertexConsumer consumer = this.getChestMaterial(entity, chestType).getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout);
            if (flag1) {
                if (chestType == ChestType.LEFT) {
                    this.render(matrices, consumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f1, i, overlay);
                } else {
                    this.render(matrices, consumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f1, i, overlay);
                }
            } else {
                this.render(matrices, consumer, this.lid, this.lock, this.bottom, f1, i, overlay);
            }

            matrices.pop();
        }
    }

    public SpriteIdentifier getChestMaterial(T t, ChestType type) {
        if (this.isChristmas) {
            return switch (type) {
                case SINGLE -> TexturedRenderLayers.CHRISTMAS;
                case LEFT -> TexturedRenderLayers.CHRISTMAS_LEFT;
                case RIGHT -> TexturedRenderLayers.CHRISTMAS_RIGHT;
            };
        } else {
            Block inventoryBlock = itemBlock;
            if (inventoryBlock == null) inventoryBlock = t.getCachedState().getBlock();
            ChestManager.ChestInfo chestInfo = ChestManager.getInfoForChest(((IChestBlock) inventoryBlock).getChestType());
            return switch (type) {
                case SINGLE -> chestInfo != null ? chestInfo.getSingleMaterial() : TexturedRenderLayers.NORMAL;
                case LEFT -> chestInfo != null ? chestInfo.getLeftMaterial() : TexturedRenderLayers.NORMAL_LEFT;
                case RIGHT -> chestInfo != null ? chestInfo.getRightMaterial() : TexturedRenderLayers.NORMAL_RIGHT;
            };
        }
    }

    public void render(MatrixStack matrixStack, VertexConsumer builder, ModelPart chestLid, ModelPart chestLatch, ModelPart chestBottom, float lidAngle, int combinedLightIn, int combinedOverlayIn) {
        chestLid.pitch = -(lidAngle * ((float) Math.PI / 2F));
        chestLatch.pitch = chestLid.pitch;
        chestLid.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
        chestLatch.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
        chestBottom.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
    }
}
