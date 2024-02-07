package net.george.blueprint.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * The {@link Model} for the slabfish patreon hat.
 * <p>For more information, visit the <a href="https://www.patreon.com/teamabnormals">Patreon</a>></p>
 */
@Environment(EnvType.CLIENT)
public class SlabfishHatModel extends Model {
    public final ModelPart body;
    public final ModelPart backpack;

    public SlabfishHatModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.body = root.getChild("body");
        this.backpack = root.getChild("backpack");
    }

    public static TexturedModelData createBodyModel() {
        ModelData modelData = new ModelData();
        ModelPartData partData = modelData.getRoot();

        ModelPartData body = partData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -18.0F, -4.0F, 10.0F, 10.0F, 4.0F, false), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        body.addChild("left_arm", ModelPartBuilder.create().uv(16, 14).cuboid(0.0F, 0.0F, -1.5F, 1.0F, 3.0F, 3.0F, false), ModelTransform.of(5.0F, -12.0F, -2.0F, 0.0F, 0.0F, -0.4363F));
        body.addChild("right_arm", ModelPartBuilder.create().uv(16, 14).cuboid(-1.0F, 0.0F, -1.5F, 1.0F, 3.0F, 3.0F, false), ModelTransform.of(-5.0F, -12.0F, -2.0F, 0.0F, 0.0F, 0.4363F));
        body.addChild("left_leg", ModelPartBuilder.create().uv(0, 14).cuboid(-1.5F, -0.0868F, -3.4924F, 3.0F, 5.0F, 3.0F, false), ModelTransform.of(2.5F, -8.0F, -1.0F, -1.3963F, 0.0F, 0.0F));
        body.addChild("right_leg", ModelPartBuilder.create().uv(0, 14).cuboid(-1.5F, -0.0868F, -3.4924F, 3.0F, 5.0F, 3.0F, false), ModelTransform.of(-2.5F, -8.0F, -1.0F, -1.3963F, 0.0F, 0.0F));
        body.addChild("fin", ModelPartBuilder.create().uv(24, 12).cuboid(0.0F, -1.0F, 0.0F, 0.0F, 4.0F, 4.0F, false), ModelTransform.of(0.0F, -12.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        partData.addChild("backpack", ModelPartBuilder.create().uv(8, 20).cuboid(-4.0F, -16.0F, 0.0F, 8.0F, 8.0F, 4.0F, false), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.pitch = x;
        modelRenderer.yaw = y;
        modelRenderer.roll = z;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {}
}
