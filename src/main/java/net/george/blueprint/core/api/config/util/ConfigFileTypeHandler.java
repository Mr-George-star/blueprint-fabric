package net.george.blueprint.core.api.config.util;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.api.config.ConfigTracker;
import net.george.blueprint.core.api.config.ModConfig;
import net.george.blueprint.core.events.ModConfigEvents;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class ConfigFileTypeHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static ConfigFileTypeHandler TOML = new ConfigFileTypeHandler();
    private static final Path defaultConfigPath = FabricLoader.getInstance().getGameDir().resolve("defaultconfigs");

    public ConfigFileTypeHandler() {
    }

    public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
        return (config) -> {
            Path configPath = configBasePath.resolve(config.getFileName());
            CommentedFileConfig configData = CommentedFileConfig.builder(configPath, TomlFormat.instance()).sync()
                    .preserveInsertionOrder().autosave().onFileNotFound((newfile, configFormat) ->
                            this.setupConfigFile(config, newfile, configFormat)).writingMode(WritingMode.REPLACE).build();
            LOGGER.debug(ConfigTracker.CONFIG, "Built TOML config for {}", configPath.toString());

            try {
                configData.load();
            } catch (ParsingException parsingException) {
                throw new ConfigLoadingException(config, parsingException);
            }

            LOGGER.debug(ConfigTracker.CONFIG, "Loaded TOML config file {}", configPath.toString());

            try {
                FileWatcher.defaultInstance().addWatch(configPath, new ConfigWatcher(config, configData, Thread.currentThread().getContextClassLoader()));
                LOGGER.debug(ConfigTracker.CONFIG, "Watching TOML config file {} for changes", configPath.toString());
                return configData;
            } catch (IOException var6) {
                throw new RuntimeException("Couldn't watch config file", var6);
            }
        };
    }

    public void unload(Path configBasePath, ModConfig config) {
        Path configPath = configBasePath.resolve(config.getFileName());

        try {
            FileWatcher.defaultInstance().removeWatch(configBasePath.resolve(config.getFileName()));
        } catch (RuntimeException var5) {
            LOGGER.error("Failed to remove config {} from tracker!", configPath.toString(), var5);
        }

    }

    private boolean setupConfigFile(ModConfig modConfig, Path file, ConfigFormat<?> conf) throws IOException {
        Files.createDirectories(file.getParent());
        Path path = defaultConfigPath.resolve(modConfig.getFileName());
        if (Files.exists(path)) {
            LOGGER.info(ConfigTracker.CONFIG, "Loading default config file from path {}", path);
            Files.copy(path, file);
        } else {
            Files.createFile(file);
            conf.initEmptyFile(file);
        }

        return true;
    }

    public static void backUpConfig(CommentedFileConfig commentedFileConfig) {
        backUpConfig(commentedFileConfig, 5);
    }

    public static void backUpConfig(CommentedFileConfig commentedFileConfig, int maxBackups) {
        Path bakFileLocation = commentedFileConfig.getNioPath().getParent();
        String bakFileName = FilenameUtils.removeExtension(commentedFileConfig.getFile().getName());
        String bakFileExtension = FilenameUtils.getExtension(commentedFileConfig.getFile().getName()) + ".bak";
        Path bakFile = bakFileLocation.resolve(bakFileName + "-1." + bakFileExtension);

        try {
            for(int i = maxBackups; i > 0; --i) {
                Path oldBak = bakFileLocation.resolve(bakFileName + "-" + i + "." + bakFileExtension);
                if (Files.exists(oldBak)) {
                    if (i >= maxBackups) {
                        Files.delete(oldBak);
                    } else {
                        Files.move(oldBak, bakFileLocation.resolve(bakFileName + "-" + (i + 1) + "." + bakFileExtension));
                    }
                }
            }

            Files.copy(commentedFileConfig.getNioPath(), bakFile);
        } catch (IOException var8) {
            LOGGER.warn(ConfigTracker.CONFIG, "Failed to back up config file {}", commentedFileConfig.getNioPath(), var8);
        }

    }

    private static class ConfigLoadingException extends RuntimeException {
        public ConfigLoadingException(ModConfig config, Exception cause) {
            super("Failed loading config file " + config.getFileName() + " of type " + config.getType() + " for modid " + config.getModId(), cause);
        }
    }

    private static class ConfigWatcher implements Runnable {
        private final ModConfig modConfig;
        private final CommentedFileConfig commentedFileConfig;
        private final ClassLoader realClassLoader;

        ConfigWatcher(ModConfig modConfig, CommentedFileConfig commentedFileConfig, ClassLoader classLoader) {
            this.modConfig = modConfig;
            this.commentedFileConfig = commentedFileConfig;
            this.realClassLoader = classLoader;
        }

        public void run() {
            Thread.currentThread().setContextClassLoader(this.realClassLoader);
            if (!this.modConfig.getSpec().isCorrecting()) {
                try {
                    this.commentedFileConfig.load();
                    if (!this.modConfig.getSpec().isCorrect(this.commentedFileConfig)) {
                        ConfigFileTypeHandler.LOGGER.warn(ConfigTracker.CONFIG, "Configuration file {} is not correct. Correcting", this.commentedFileConfig.getFile().getAbsolutePath());
                        ConfigFileTypeHandler.backUpConfig(this.commentedFileConfig);
                        this.modConfig.getSpec().correct(this.commentedFileConfig);
                        this.commentedFileConfig.save();
                    }
                } catch (ParsingException exception) {
                    throw new ConfigLoadingException(this.modConfig, exception);
                }

                ConfigFileTypeHandler.LOGGER.debug(ConfigTracker.CONFIG, "Config file {} changed, sending notifies", this.modConfig.getFileName());
                this.modConfig.getSpec().afterReload();
                ModConfigEvents.RELOADING.invoker().onModConfigReloading(this.modConfig);
            }

        }
    }
}
