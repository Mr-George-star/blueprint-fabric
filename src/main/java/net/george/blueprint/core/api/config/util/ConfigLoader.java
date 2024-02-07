package net.george.blueprint.core.api.config.util;

import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.Blueprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class ConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void loadDefaultConfigPath() {
        LOGGER.trace(Blueprint.CORE, "Default config paths at {}", ConfigPaths.DEFAULT_CONFIGS_PATH);
        FileUtil.getOrCreateDirectory(FabricLoader.getInstance().getGameDir().resolve(ConfigPaths.DEFAULT_CONFIGS_PATH), "default config directory");
    }
}
