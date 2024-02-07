package net.george.blueprint.core.endimator;

import net.george.blueprint.core.endimator.model.EndimatorPartPose;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.Map;

/**
 * An extension of vanilla's {@link ModelPart} class that allows for scaling and relative offsetting of parts.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public class EndimatorModelPart extends ModelPart implements EndimatablePart {
    public float xOffset, yOffset, zOffset;
    public float xScale = 1.0F, yScale = 1.0F, zScale = 1.0F;
    public boolean scaleChildren = true;

    public EndimatorModelPart(List<Cuboid> cubes, Map<String, ModelPart> children) {
        super(cubes, children);
    }

    public EndimatorModelPart(ModelPart part) {
        super(part.cuboids, part.children);
        this.copyTransform(part);
    }

    public EndimatorModelPart(ModelPart part, float xOffset, float yOffset, float zOffset, float xScale, float yScale, float zScale, boolean scaleChildren) {
        this(part.cuboids, part.children);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
        this.scaleChildren = scaleChildren;
    }

    public EndimatorModelPart(ModelPart part, float xOffset, float yOffset, float zOffset) {
        this(part.cuboids, part.children);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public EndimatorModelPart(ModelPart part, float xScale, float yScale, float zScale, boolean scaleChildren) {
        this(part.cuboids, part.children);
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
        this.scaleChildren = scaleChildren;
    }

    /**
     * Sets the scale dimensions of this part.
     *
     * @param x The new x scale.
     * @param y The new y scale.
     * @param z The new z scale.
     */
    public void setScale(float x, float y, float z) {
        this.xScale = x;
        this.yScale = y;
        this.zScale = z;
    }

    /**
     * Sets if this part should scale its children with its own scale.
     *
     * @param scaleChildren If this part should scale its children with its own scale.
     */
    public void setShouldScaleChildren(boolean scaleChildren) {
        this.scaleChildren = scaleChildren;
    }

    /**
     * Sets the dimensional offsets of this part.
     *
     * @param x The new x offset.
     * @param y The new y offset.
     * @param z The new z offset.
     */
    public void setOffset(float x, float y, float z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    @Override
    public void render(MatrixStack pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        if (this.visible) {
            Map<String, ModelPart> children = this.children;
            if (!this.cuboids.isEmpty() || !children.isEmpty()) {
                pose.push();
                this.rotate(pose);
                if (this.scaleChildren) {
                    pose.translate(this.xOffset, this.yOffset, this.zOffset);
                    pose.scale(this.xScale, this.yScale, this.zScale);
                    this.compile(pose.peek(), consumer, light, overlay, red, green, blue, alpha);
                } else {
                    pose.push();
                    pose.translate(this.xOffset, this.yOffset, this.zOffset);
                    pose.scale(this.xScale, this.yScale, this.zScale);
                    this.compile(pose.peek(), consumer, light, overlay, red, green, blue, alpha);
                    pose.pop();
                }
                for (ModelPart part : children.values()) {
                    part.render(pose, consumer, light, overlay, red, green, blue, alpha);
                }
                pose.pop();
            }
        }
    }

    private void compile(MatrixStack.Entry entry, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        for (Cuboid cube : this.cuboids) {
            cube.renderCuboid(entry, consumer, light, overlay, red, green, blue, alpha);
        }
    }

    @Override
    public void addOffset(float x, float y, float z) {
        this.xOffset -= x;
        this.yOffset -= y;
        this.zOffset -= z;
    }

    @Override
    public void addScale(float x, float y, float z) {
        this.xScale += x;
        this.yScale += y;
        this.zScale += z;
    }

    /**
     * Loads the pose values of an {@link EndimatorPartPose} instance onto this part.
     *
     * @param endimatorPartPose An {@link EndimatorPartPose} instance to load its pose values onto this part.
     */
    public void loadPose(EndimatorPartPose endimatorPartPose) {
        this.setTransform(endimatorPartPose.partPose);
        this.setOffset(endimatorPartPose.offsetX, endimatorPartPose.offsetY, endimatorPartPose.offsetZ);
        this.setScale(endimatorPartPose.scaleX, endimatorPartPose.scaleY, endimatorPartPose.scaleZ);
    }
}