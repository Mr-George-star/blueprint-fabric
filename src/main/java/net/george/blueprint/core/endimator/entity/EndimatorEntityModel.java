package net.george.blueprint.core.endimator.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.core.endimator.Endimatable;
import net.george.blueprint.core.endimator.Endimation;
import net.george.blueprint.core.endimator.Endimator;
import net.george.blueprint.core.endimator.PlayableEndimation;
import net.george.blueprint.client.ClientInfo;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;

import java.util.HashMap;

/**
 * An {@link EntityModel} extension that simplifies the animation of {@link Endimatable} entities.
 *
 * @param <E> The type of entity for the model.
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public abstract class EndimatorEntityModel<E extends Entity & Endimatable> extends EntityModel<E> {
    protected Endimator endimator = new Endimator(new HashMap<>());
    protected E entity;

    public EndimatorEntityModel() {
        super();
    }

    /**
     * Updates this model's {@link #endimator}.
     *
     * @param endimatedEntity The entity to animate for.
     */
    public void animateModel(E endimatedEntity, float partialTicks) {
        PlayableEndimation playingEndimation = endimatedEntity.getPlayingEndimation();
        Endimation endimation = playingEndimation.asEndimation();
        if (endimation != null) {
            float time = (endimatedEntity.getAnimationTick() + partialTicks) * 0.05F;
            float length = endimation.getLength();
            if (time > length) {
                time = length;
            }
            this.endimator.apply(endimation, time, Endimator.ResetMode.ALL);
            endimatedEntity.getEffectHandler().update(endimation, time);
        }
    }

    @Override
    public void setAngles(E entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.entity = entity;
        this.animateModel(entity, ClientInfo.getPartialTicks());
    }
}
