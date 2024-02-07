package net.george.blueprint.core.util.extension;

import net.minecraft.client.MinecraftClient;

public interface ScreenExtension {
    default MinecraftClient getClient() {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }
}
