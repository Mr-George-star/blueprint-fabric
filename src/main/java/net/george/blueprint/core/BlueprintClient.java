package net.george.blueprint.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.george.blueprint.client.ChestManager;
import net.george.blueprint.client.screen.shake.ScreenShakeHandler;
import net.george.blueprint.common.server.LogicalSidedProvider;
import net.george.blueprint.core.api.SignManager;
import net.george.blueprint.core.api.config.network.ConfigSyncClient;

public class BlueprintClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigSyncClient.INSTANCE.clientInit();
        ScreenShakeHandler.registerEvents();
        ChestManager.registerEvents();
        SignManager.setupAtlas();

        this.registerCallbacks();
    }

    private void registerCallbacks() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> LogicalSidedProvider.setClient(() -> client));
    }
}
