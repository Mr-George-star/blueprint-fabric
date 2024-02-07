package net.george.blueprint.core.mixin.client;

import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.core.util.extension.ScreenExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenExtension {
    @Shadow @Nullable
    protected MinecraftClient client;

    @Override
    public MinecraftClient getClient() {
        return this.client == null ? ClientInfo.MINECRAFT : this.client;
    }
}
