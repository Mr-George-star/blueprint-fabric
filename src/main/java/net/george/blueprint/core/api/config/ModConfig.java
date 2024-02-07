package net.george.blueprint.core.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.ModContainer;
import net.george.blueprint.core.api.config.util.ConfigFileTypeHandler;
import net.george.blueprint.core.events.ModConfigEvents;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

@SuppressWarnings({"unused", "unchecked"})
public class ModConfig {
    private final Type type;
    private final IConfigSpec<?> spec;
    private final String fileName;
    private final ModContainer container;
    private final ConfigFileTypeHandler configHandler;
    private CommentedConfig configData;
    private Callable<Void> saveHandler;

    public ModConfig(Type type, IConfigSpec<?> spec, ModContainer container, String fileName) {
        this.type = type;
        this.spec = spec;
        this.fileName = fileName;
        this.container = container;
        this.configHandler = ConfigFileTypeHandler.TOML;
        ConfigTracker.INSTANCE.trackConfig(this);
    }

    public ModConfig(Type type, IConfigSpec<?> spec, ModContainer activeContainer) {
        this(type, spec, activeContainer, defaultConfigName(type, activeContainer.getMetadata().getId()));
    }

    private static String defaultConfigName(Type type, String modId) {
        return String.format("%s-%s.toml", modId, type.extension());
    }

    public Type getType() {
        return this.type;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ConfigFileTypeHandler getHandler() {
        return this.configHandler;
    }

    public <T extends IConfigSpec<T>> IConfigSpec<T> getSpec() {
        return (IConfigSpec<T>)this.spec;
    }

    public String getModId() {
        return this.container.getMetadata().getId();
    }

    public CommentedConfig getConfigData() {
        return this.configData;
    }

    void setConfigData(CommentedConfig configData) {
        this.configData = configData;
        this.spec.acceptConfig(this.configData);
    }

    public void save() {
        ((CommentedFileConfig)this.configData).save();
    }

    public Path getFullPath() {
        return ((CommentedFileConfig)this.configData).getNioPath();
    }

    public void acceptSyncedConfig(byte[] bytes) {
        this.setConfigData(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(bytes)));
        ModConfigEvents.RELOADING.invoker().onModConfigReloading(this);
    }

    public enum Type {
        COMMON,
        CLIENT,
        SERVER;

        public String extension() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
