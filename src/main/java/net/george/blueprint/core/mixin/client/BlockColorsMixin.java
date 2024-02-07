package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.Blueprint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    @Inject(method = "create", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private static void create(CallbackInfoReturnable<BlockColors> cir) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManagerImpl) {
            ((ReloadableResourceManagerImpl) resourceManager).registerReloader(Blueprint.ENDIMATION_LOADER);
        }
    }
}
