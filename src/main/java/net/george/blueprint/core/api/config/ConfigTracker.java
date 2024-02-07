package net.george.blueprint.core.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.events.ModConfigEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused"})
public class ConfigTracker implements IConfigTracker {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Marker CONFIG = MarkerManager.getMarker("CONFIG");
    public static final ConfigTracker INSTANCE = new ConfigTracker();
    private final ConcurrentHashMap<String, ModConfig> fileMap = new ConcurrentHashMap<>();
    private final EnumMap<ModConfig.Type, Set<ModConfig>> configSets = new EnumMap<>(ModConfig.Type.class);
    private final ConcurrentHashMap<String, Map<ModConfig.Type, ModConfig>> configsByMod = new ConcurrentHashMap<>();

    private ConfigTracker() {
        this.configSets.put(ModConfig.Type.CLIENT, Collections.synchronizedSet(new LinkedHashSet<>()));
        this.configSets.put(ModConfig.Type.COMMON, Collections.synchronizedSet(new LinkedHashSet<>()));
        this.configSets.put(ModConfig.Type.SERVER, Collections.synchronizedSet(new LinkedHashSet<>()));
    }

    void trackConfig(ModConfig config) {
        if (this.fileMap.containsKey(config.getFileName())) {
            LOGGER.error(CONFIG, "Detected config file conflict {} between {} and {}", config.getFileName(),
                    this.fileMap.get(config.getFileName()).getModId(), config.getModId());
            throw new RuntimeException("Config conflict detected!");
        } else {
            this.fileMap.put(config.getFileName(), config);
            (this.configSets.get(config.getType())).add(config);
            (this.configsByMod.computeIfAbsent(config.getModId(), (k) -> new EnumMap<>(ModConfig.Type.class))).put(config.getType(), config);
            LOGGER.debug(CONFIG, "Config file {} for {} tracking", config.getFileName(), config.getModId());
            this.loadConfig(config, FabricLoader.getInstance().getConfigDir());
        }
    }

    private void loadConfig(ModConfig config, Path configBasePath) {
        if (config.getType() != ModConfig.Type.SERVER) {
            this.openConfig(config, configBasePath);
        }

    }

    public void loadConfigs(ModConfig.Type type, Path configBasePath) {
        LOGGER.debug(CONFIG, "Loading configs type {}", type);
        (this.configSets.get(type)).forEach((config) -> this.openConfig(config, configBasePath));
    }

    public void unloadConfigs(ModConfig.Type type, Path configBasePath) {
        LOGGER.debug(CONFIG, "Unloading configs type {}", type);
        (this.configSets.get(type)).forEach((config) -> this.closeConfig(config, configBasePath));
    }

    private void openConfig(ModConfig config, Path configBasePath) {
        LOGGER.trace(CONFIG, "Loading config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
        CommentedFileConfig configData = config.getHandler().reader(configBasePath).apply(config);
        config.setConfigData(configData);
        ModConfigEvents.LOADING.invoker().onModConfigLoading(config);
        config.save();
    }

    private void closeConfig(ModConfig config, Path configBasePath) {
        if (config.getConfigData() != null) {
            LOGGER.trace(CONFIG, "Closing config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
            config.save();
            config.getHandler().unload(configBasePath, config);
            config.setConfigData(null);
        }

    }

    public void loadDefaultServerConfigs() {
        (this.configSets.get(ModConfig.Type.SERVER)).forEach((modConfig) -> {
            CommentedConfig commentedConfig = CommentedConfig.inMemory();
            modConfig.getSpec().correct(commentedConfig);
            modConfig.setConfigData(commentedConfig);
            ModConfigEvents.LOADING.invoker().onModConfigLoading(modConfig);
        });
    }

    public String getConfigFileName(String modId, ModConfig.Type type) {
        return Optional.ofNullable((this.configsByMod.getOrDefault(modId, Collections.emptyMap())).
                getOrDefault(type, null)).map(ModConfig::getFullPath).map(Object::toString).orElse(null);
    }

    public Map<ModConfig.Type, Set<ModConfig>> configSets() {
        return this.configSets;
    }

    public ConcurrentHashMap<String, ModConfig> fileMap() {
        return this.fileMap;
    }
}
