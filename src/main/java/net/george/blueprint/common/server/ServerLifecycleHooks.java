package net.george.blueprint.common.server;

import net.george.blueprint.core.api.config.ConfigTracker;
import net.george.blueprint.core.api.config.ModConfig;
import net.george.blueprint.core.api.config.util.ConfigPaths;
import net.george.blueprint.core.api.config.util.FileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class ServerLifecycleHooks {
    private static final WorldSavePath SERVER_CONFIG = new WorldSavePath(ConfigPaths.SERVER_CONFIG_PATH);
    private static MinecraftServer currentServer;


    private static Path getServerConfigPath(MinecraftServer server) {
        Path serverConfig = server.getSavePath(SERVER_CONFIG);
        FileUtil.getOrCreateDirectory(serverConfig, "server config directory");
        return serverConfig;
    }

    public static void handleServerAboutToStart(MinecraftServer server) {
        currentServer = server;
        LogicalSidedProvider.setServer(() -> server);
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, getServerConfigPath(server));
    }

    public static void handleServerStopped(MinecraftServer server) {
        currentServer = null;
        LogicalSidedProvider.setServer(null);
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.SERVER, getServerConfigPath(server));
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
