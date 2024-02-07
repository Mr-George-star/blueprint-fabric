package net.george.blueprint.core.api.config;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public interface IConfigTracker {
    IConfigTracker INSTANCE = ConfigTracker.INSTANCE;

    Map<ModConfig.Type, Set<ModConfig>> configSets();

    ConcurrentHashMap<String, ModConfig> fileMap();
}
