package net.george.blueprint.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.common.entity.BlueprintBoat;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

/**
 * The {@link EntityRenderer} responsible for the rendering of Blueprint's boat entities.
 */
@Environment(EnvType.CLIENT)
public class BlueprintBoatRenderer extends EntityRenderer<BlueprintBoat> {
    private final BoatEntityModel model;

    public BlueprintBoatRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new BoatEntityModel(BoatEntityModel.getTexturedModelData().createModel());
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(BlueprintBoat entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0D, 0.375D, 0.0D);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F - yaw));
        float f = (float) entity.getDamageWobbleTicks() - tickDelta;
        float f1 = entity.getDamageWobbleStrength() - tickDelta;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(f) * f * f1 / 10.0F * (float) entity.getDamageWobbleSide()));
        }

        float f2 = entity.interpolateBubbleWobble(tickDelta);
        if (!MathHelper.approximatelyEquals(f2, 0.0F)) {
            matrices.multiply(new Quaternion(new Vec3f(1.0F, 0.0F, 1.0F), entity.interpolateBubbleWobble(tickDelta), true));
        }

        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
        this.model.setAngles(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        VertexConsumer vertexConsumer1 = vertexConsumers.getBuffer(RenderLayer.getWaterMask());
        this.model.getWaterPatch().render(matrices, vertexConsumer1, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(BlueprintBoat entity) {
        return entity.getBoat().getTexture();
    }
}
