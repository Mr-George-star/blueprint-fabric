package net.george.blueprint.common.network;

import net.george.blueprint.common.network.entity.SpawnEntityS2CPacket;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.config.ConfigTracker;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class NetworkHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isVanillaConnection = true;

    public static void setModdedConnection() {
        isVanillaConnection = false;
    }

    private static void setVanillaConnection() {
        isVanillaConnection = true;
    }

    public static boolean isVanillaConnection(ClientConnection manager) {
        return isVanillaConnection;
    }

    public static void handleClientLoginSuccess(ClientConnection manager) {
        if (isVanillaConnection(manager)) {
            LOGGER.info("Connected to a vanilla server. Catching up missing behaviour.");
            ConfigTracker.INSTANCE.loadDefaultServerConfigs();
        } else {
            setVanillaConnection();
            LOGGER.info("Connected to a modded server.");
        }
    }

    public static Packet<?> getEntitySpawningPacket(Entity entity) {
        return Blueprint.PLAY_CHANNEL.createVanillaPacket(new SpawnEntityS2CPacket(entity));
    }
}
