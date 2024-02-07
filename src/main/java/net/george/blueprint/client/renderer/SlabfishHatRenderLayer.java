package net.george.blueprint.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.client.RewardHandler;
import net.george.blueprint.client.model.SlabfishHatModel;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.sonar.OnlineImageCache;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.concurrent.TimeUnit;

/**
 * The {@link RenderLayer} responsible for the rendering of the slabfish patreon hats.
 * <p>For more information, visit the <a href="https://www.patreon.com/teamabnormals">Patreon</a>></p>
 */
@Environment(EnvType.CLIENT)
public class SlabfishHatRenderLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public static OnlineImageCache REWARD_CACHE = new OnlineImageCache(Blueprint.MOD_ID, 1, TimeUnit.DAYS);
    private final SlabfishHatModel model;

    public SlabfishHatRenderLayer(PlayerEntityRenderer renderer) {
        super(renderer);
        this.model = new SlabfishHatModel(SlabfishHatModel.createBodyModel().createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        RewardHandler.RewardProperties properties = RewardHandler.getRewardProperties();
        if (properties == null)
            return;

        RewardHandler.RewardProperties.SlabfishProperties slabfishProperties = properties.getSlabfishProperties();
        if (slabfishProperties == null)
            return;

        String defaultTypeUrl = slabfishProperties.getDefaultTypeUrl();
        IDataManager data = (IDataManager) entity;

        if (entity.isInvisible() || entity.isSpectator() || !(RewardHandler.SlabfishSetting.getSetting(data, RewardHandler.SlabfishSetting.ENABLED)) || defaultTypeUrl == null || !RewardHandler.REWARDS.containsKey(entity.getUuid()))
            return;

        RewardHandler.RewardData reward = RewardHandler.REWARDS.get(entity.getUuid());

        if (reward.getSlabfish() == null || reward.getTier() < 2)
            return;

        RewardHandler.RewardData.SlabfishData slabfish = reward.getSlabfish();
        Identifier typeLocation = REWARD_CACHE.requestTexture(reward.getTier() >= 4 && slabfish.getTypeUrl() != null && RewardHandler.SlabfishSetting.getSetting(data, RewardHandler.SlabfishSetting.TYPE) ? slabfish.getTypeUrl() : defaultTypeUrl).getNow(null);
        if (typeLocation == null)
            return;

        Identifier sweaterLocation = reward.getTier() >= 3 && slabfish.getSweaterUrl() != null && RewardHandler.SlabfishSetting.getSetting(data, RewardHandler.SlabfishSetting.SWEATER) ? REWARD_CACHE.requestTexture(slabfish.getSweaterUrl()).getNow(null) : null;
        Identifier backpackLocation = slabfish.getBackpackUrl() != null && RewardHandler.SlabfishSetting.getSetting(data, RewardHandler.SlabfishSetting.BACKPACK) ? REWARD_CACHE.requestTexture(slabfish.getBackpackUrl()).getNow(null) : null;
        ModelPart body = this.model.body;
        ModelPart backpack = this.model.backpack;

        body.copyTransform(this.getContextModel().head);
        body.render(matrices, vertexConsumers.getBuffer(slabfish.isTranslucent() ? RenderLayer.getEntityTranslucent(typeLocation) : RenderLayer.getEntityCutout(typeLocation)), light, OverlayTexture.DEFAULT_UV);

        if (sweaterLocation != null)
            body.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(sweaterLocation)), light, OverlayTexture.DEFAULT_UV);

        if (backpackLocation != null) {
            backpack.copyTransform(body);
            backpack.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(backpackLocation)), light, OverlayTexture.DEFAULT_UV);
        }
    }
}
