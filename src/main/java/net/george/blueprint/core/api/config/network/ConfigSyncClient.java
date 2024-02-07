package net.george.blueprint.core.api.config.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.george.blueprint.common.network.NetworkHooks;
import net.george.blueprint.core.api.config.ConfigTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ConfigSyncClient {
    public static final ConfigSyncClient INSTANCE;
    private final ConfigTracker tracker;

    private ConfigSyncClient(ConfigTracker tracker) {
        this.tracker = tracker;
    }

    public void clientInit() {
        ClientLoginNetworking.registerGlobalReceiver(ConfigSync.SYNC_CONFIGS_CHANNEL, (client, handler, buf, listenerAdder) -> {
            String fileName = this.receiveSyncedConfig(buf);
            ConfigSync.LOGGER.debug(ConfigSync.FMLHSMARKER, "Received config sync for {} from server", fileName);
            PacketByteBuf response = PacketByteBufs.create();
            response.writeString(fileName);
            ConfigSync.LOGGER.debug(ConfigSync.FMLHSMARKER, "Sent config sync for {} to server", fileName);
            return CompletableFuture.completedFuture(response);
        });
        ClientLoginNetworking.registerGlobalReceiver(ConfigSync.MODDED_CONNECTION_CHANNEL, (client, handler, buf, listenerAdder) -> {
            ConfigSync.LOGGER.debug(ConfigSync.FMLHSMARKER, "Received modded connection marker from server");
            NetworkHooks.setModdedConnection();
            return CompletableFuture.completedFuture(PacketByteBufs.create());
        });
    }

    private String receiveSyncedConfig(PacketByteBuf buf) {
        String fileName = buf.readString(32767);
        byte[] fileData = buf.readByteArray();
        if (!MinecraftClient.getInstance().isInSingleplayer()) {
            Optional.ofNullable(this.tracker.fileMap().get(fileName)).ifPresent((config) -> config.acceptSyncedConfig(fileData));
        }

        return fileName;
    }

    static {
        INSTANCE = new ConfigSyncClient(ConfigTracker.INSTANCE);
    }
}
