package net.george.blueprint.core.endimator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.george.blueprint.core.Blueprint;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Handles the data-driven internals for {@link Endimation} instances.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings({"deprecation", "OptionalGetWithoutIsPresent", "unused"})
public final class EndimationLoader implements ResourceReloader {
    private static final JsonParser PARSER = new JsonParser();
    private final BiMap<Identifier, Endimation> registry = HashBiMap.create();

    /**
     * Gets the {@link Endimation} mapped to a given {@link Identifier} key.
     *
     * @param key A {@link Identifier} key to use to look up its {@link Endimation}.
     * @return The {@link Endimation} mapped to a given {@link Identifier} key, or null if no such mapping exists.
     */
    @Nullable
    public Endimation getEndimation(Identifier key) {
        return this.registry.get(key);
    }

    /**
     * Gets {@link Identifier} key for a given {@link Endimation}.
     *
     * @param endimation An {@link Endimation} to use to look up its {@link Identifier} key.
     * @return The {@link Identifier} key for a given {@link Endimation}, or null if no such key exists.
     */
    @Nullable
    public Identifier getKey(Endimation endimation) {
        return this.registry.inverse().get(endimation);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, Endimation> endimations = new HashMap<>();
            for (Identifier Identifier : manager.findResources("endimations", (file) -> file.endsWith(".json"))) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(manager.getResource(Identifier).getInputStream())) {
                    var dataResult = Endimation.CODEC.decode(JsonOps.INSTANCE, PARSER.parse(inputStreamReader));
                    var error = dataResult.error();
                    if (error.isPresent()) {
                        throw new JsonParseException(error.get().message());
                    } else {
                        String path = Identifier.getPath();
                        Identifier adjustedLocation = new Identifier(Identifier.getNamespace(), path.substring(12, path.length() - 5));
                        if (endimations.put(adjustedLocation, dataResult.result().get().getFirst()) != null) {
                            Blueprint.LOGGER.warn("Loaded Duplicate Endimation: {}", adjustedLocation);
                        }
                    }
                } catch (Exception exception) {
                    Blueprint.LOGGER.error("Error while loading Endimation: {}", Identifier, exception);
                }
            }
            return endimations;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(endimations -> {
            BiMap<Identifier, Endimation> registry = this.registry;
            registry.clear();
            registry.putAll(endimations);
            registry.put(PlayableEndimation.BLANK.identifier(), Endimation.BLANK);
            Blueprint.LOGGER.info("Endimation Loader has loaded {} endimations", registry.size());
        }, applyExecutor);
    }
}
