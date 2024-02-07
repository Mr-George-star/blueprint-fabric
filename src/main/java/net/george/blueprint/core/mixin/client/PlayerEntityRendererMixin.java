package net.george.blueprint.core.mixin.client;

import net.george.blueprint.client.RewardHandler;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.core.events.RenderPlayerEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        RenderPlayerEvents.POST.invoker().interact(abstractClientPlayerEntity, g, matrixStack, vertexConsumerProvider, i);
    }

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    public void renderLabelIfPresent(AbstractClientPlayerEntity entity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        RewardHandler.RewardProperties properties = RewardHandler.getRewardProperties();
        if (properties != null) {
            RewardHandler.RewardProperties.SlabfishProperties slabfishProperties = properties.getSlabfishProperties();
            if (slabfishProperties != null) {
                if (RewardHandler.SlabfishSetting.getSetting((IDataManager) entity, RewardHandler.SlabfishSetting.ENABLED)) {
                    UUID uuid = entity.getUuid();
                    if (RewardHandler.REWARDS.containsKey(uuid)) {
                        RewardHandler.RewardData reward = RewardHandler.REWARDS.get(uuid);
                        RewardHandler.RewardData.SlabfishData slabfish = reward.getSlabfish();
                        int tier = reward.getTier();
                        if (slabfish != null && tier >= 2 && (slabfish.getTypeUrl() != null || tier <= 3 || slabfishProperties.getDefaultTypeUrl() != null) && slabfishProperties.getDefaultTypeUrl() != null) {
                            matrixStack.translate(0.0, 0.5, 0.0);
                        }
                    }
                }
            }
        }
    }
}
