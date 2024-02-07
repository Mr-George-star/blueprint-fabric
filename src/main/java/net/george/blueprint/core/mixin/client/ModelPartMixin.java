package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.endimator.EndimatablePart;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.class)
public class ModelPartMixin implements EndimatablePart {
    @Shadow
    public float pivotX;
    @Shadow
    public float pivotY;
    @Shadow
    public float pivotZ;
    @Shadow
    public float pitch;
    @Shadow
    public float yaw;
    @Shadow
    public float roll;

    @Override
    public void addPos(float x, float y, float z) {
        this.pivotX -= x;
        this.pivotY -= y;
        this.pivotZ -= z;
    }

    @Override
    public void addRotation(float x, float y, float z) {
        this.pitch += x;
        this.yaw += y;
        this.roll += z;
    }
}
