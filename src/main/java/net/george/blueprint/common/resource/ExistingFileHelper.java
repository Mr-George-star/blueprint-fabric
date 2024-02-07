package net.george.blueprint.common.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.client.resource.DefaultClientResourcePack;
import net.minecraft.client.resource.ResourceIndex;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Enables data providers to check if other data files currently exist.
 */
@SuppressWarnings("unused")
public class ExistingFileHelper {
    public interface IResourceType {
        ResourceType getResourceType();

        String getSuffix();

        String getPrefix();
    }

    public static class ModdedResourceType implements IResourceType {
        final ResourceType resourceType;
        final String suffix, prefix;

        public ModdedResourceType(ResourceType type, String suffix, String prefix) {
            this.resourceType = type;
            this.suffix = suffix;
            this.prefix = prefix;
        }

        @Override
        public ResourceType getResourceType() {
            return this.resourceType;
        }

        @Override
        public String getSuffix() {
            return this.suffix;
        }

        @Override
        public String getPrefix() {
            return this.prefix;
        }
    }

    private final LifecycledResourceManagerImpl clientResources, serverData;
    private final boolean enable;
    private final Multimap<ResourceType, Identifier> generated = HashMultimap.create();

    public static final String EXISTING_RESOURCES = "blueprint.datagen.existing_resources";

    /**
     * Create a helper with existing resources provided from a JVM argument.
     * To use, a JVM argument mapping {@link ExistingFileHelper#EXISTING_RESOURCES the key}
     * to the desired resource directory is required.
     */
    public static ExistingFileHelper withResourcesFromArg() {
        String property = System.getProperty(EXISTING_RESOURCES);
        if (property == null) {
            throw new IllegalArgumentException("Existing resources not specified with '" + EXISTING_RESOURCES + "' argument");
        }
        Path path = Paths.get(property);
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("Path " + property + " is not a directory or does not exist");
        }
        return withResources(path);
    }

    /**
     * Create a helper for a standard mod environment.
     * Assumes a file tree of: <pre>
     *     - root
     *         - run
     *     - src
     *         - main
     *             - resources
     * </pre>
     * @deprecated use withResourcesFromArg
     */
    @Deprecated(forRemoval = true)
    public static ExistingFileHelper standard() {
        return withResources(FabricLoader.getInstance()
                .getGameDir()
                .normalize()
                .getParent() // root
                .resolve("src")
                .resolve("main")
                .resolve("resources")
        );
    }

    /**
     * Create a helper with the provided paths being used for resources.
     */
    public static ExistingFileHelper withResources(Path... paths) {
        List<Path> resources = List.of(paths);
        return new ExistingFileHelper(resources, Set.of(), true, null, null);
    }

    public ExistingFileHelper(Collection<Path> existingPacks, Set<String> existingMods, boolean enable, @Nullable String assetIndex, @Nullable File assetsDir) {
        List<ResourcePack> candidateClientResources = new ArrayList<>();
        List<ResourcePack> candidateServerResources = new ArrayList<>();

        candidateClientResources.add(new DefaultResourcePack(ClientBuiltinResourcePackProvider.DEFAULT_PACK_METADATA, "minecraft", "realms"));
        if (assetIndex != null && assetsDir != null) {
            candidateClientResources.add(new DefaultClientResourcePack(ClientBuiltinResourcePackProvider.DEFAULT_PACK_METADATA, new ResourceIndex(assetsDir, assetIndex)));
        }
        candidateServerResources.add(new DefaultResourcePack(VanillaDataPackProvider.DEFAULT_PACK_METADATA, "minecraft"));
        for (Path existing : existingPacks) {
            File file = existing.toFile();
            ResourcePack pack = file.isDirectory() ? new DirectoryResourcePack(file) : new ZipResourcePack(file);
            candidateClientResources.add(pack);
            candidateServerResources.add(pack);
        }
        for (String existingMod : existingMods) {
            ModContainer modFileInfo = FabricLoader.getInstance().getModContainer(existingMod).orElse(null);
            if (modFileInfo != null) {
                ResourcePack pack = ResourcePackLoader.createPackForMod(modFileInfo);
                candidateClientResources.add(pack);
                candidateServerResources.add(pack);
            }
        }

        this.clientResources = new LifecycledResourceManagerImpl(ResourceType.CLIENT_RESOURCES, candidateClientResources);
        this.serverData = new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, candidateServerResources);
        this.enable = enable;
    }

    private ResourceManager getManager(ResourceType resourceType) {
        return resourceType == ResourceType.CLIENT_RESOURCES ? this.clientResources : this.serverData;
    }

    private Identifier getLocation(Identifier base, String suffix, String prefix) {
        return new Identifier(base.getNamespace(), prefix + "/" + base.getPath() + suffix);
    }

    /**
     * Check if a given resource exists in the known resource packs.
     *
     * @param id      the complete location of the resource, e.g.
     *                 {@code "minecraft:textures/block/stone.png"}
     * @param resourceType the type of resources to check
     * @return {@code true} if the resource exists in any pack, {@code false}
     *         otherwise
     */
    public boolean exists(Identifier id, ResourceType resourceType) {
        if (!enable) {
            return true;
        }
        return generated.get(resourceType).contains(id) || getManager(resourceType).containsResource(id);
    }

    /**
     * Check if a given resource exists in the known resource packs. This is a
     * convenience method to avoid repeating type/prefix/suffix and instead use the
     * common definitions in {@link ResourceType}, or a custom {@link IResourceType}
     * definition.
     *
     * @param id  the base location of the resource, e.g.
     *             {@code "minecraft:block/stone"}
     * @param type a {@link IResourceType} describing how to form the path to the
     *             resource
     * @return {@code true} if the resource exists in any pack, {@code false}
     *         otherwise
     */
    public boolean exists(Identifier id, IResourceType type) {
        return exists(getLocation(id, type.getSuffix(), type.getPrefix()), type.getResourceType());
    }

    /**
     * Check if a given resource exists in the known resource packs.
     *
     * @param loc        the base location of the resource, e.g.
     *                   {@code "minecraft:block/stone"}
     * @param resourceType   the type of resources to check
     * @param pathSuffix a string to append after the path, e.g. {@code ".json"}
     * @param pathPrefix a string to append before the path, before a slash, e.g.
     *                   {@code "models"}
     * @return {@code true} if the resource exists in any pack, {@code false}
     *         otherwise
     */
    public boolean exists(Identifier loc, ResourceType resourceType, String pathSuffix, String pathPrefix) {
        return exists(getLocation(loc, pathSuffix, pathPrefix), resourceType);
    }

    /**
     * Track the existence of a generated file. This is a convenience method to
     * avoid repeating type/prefix/suffix and instead use the common definitions in
     * {@link ResourceType}, or a custom {@link IResourceType} definition.
     * <p>
     * This should be called by data providers immediately when a new data object is
     * created, i.e. not during
     * {@link DataProvider#run(DataCache) run} but instead
     * when the "builder" (or whatever intermediate object) is created, such as a
     * {@link ModelBuilder}.
     * <p>
     * This represents a <em>promise</em> to generate the file later, since other
     * datagen may rely on this file existing.
     *
     * @param id  the base location of the resource, e.g.
     *             {@code "minecraft:block/stone"}
     * @param type a {@link IResourceType} describing how to form the path to the
     *             resource
     */
    public void trackGenerated(Identifier id, IResourceType type) {
        this.generated.put(type.getResourceType(), getLocation(id, type.getSuffix(), type.getPrefix()));
    }

    /**
     * Track the existence of a generated file.
     * <p>
     * This should be called by data providers immediately when a new data object is
     * created, i.e. not during
     * {@link DataProvider#run(DataCache) run} but instead
     * when the "builder" (or whatever intermediate object) is created, such as a
     * {@link ModelBuilder}.
     * <p>
     * This represents a <em>promise</em> to generate the file later, since other
     * datagen may rely on this file existing.
     *
     * @param id        the base location of the resource, e.g.
     *                   {@code "minecraft:block/stone"}
     * @param resourceType   the type of resources to check
     * @param pathSuffix a string to append after the path, e.g. {@code ".json"}
     * @param pathPrefix a string to append before the path, before a slash, e.g.
     *                   {@code "models"}
     */
    public void trackGenerated(Identifier id, ResourceType resourceType, String pathSuffix, String pathPrefix) {
        this.generated.put(resourceType, getLocation(id, pathSuffix, pathPrefix));
    }

    @VisibleForTesting
    public Resource getResource(Identifier id, ResourceType resourceType, String pathSuffix, String pathPrefix) throws IOException {
        return getResource(getLocation(id, pathSuffix, pathPrefix), resourceType);
    }

    @VisibleForTesting
    public Resource getResource(Identifier id, ResourceType resourceType) throws IOException {
        return getManager(resourceType).getResource(id);
    }

    /**
     * @return {@code true} if validation is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return this.enable;
    }
}
