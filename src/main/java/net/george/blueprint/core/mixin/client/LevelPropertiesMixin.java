package net.george.blueprint.core.mixin.client;

import com.mojang.serialization.Lifecycle;
import net.george.blueprint.core.BlueprintConfig;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public class LevelPropertiesMixin {
    @Inject(method = "getLifecycle", at = @At("HEAD"), cancellable = true)
    public void getLifecycle(CallbackInfoReturnable<Lifecycle> cir) {
        if (BlueprintConfig.CLIENT.disableExperimentalSettingsScreen) {
            cir.setReturnValue(Lifecycle.stable());
        }
    }
}
