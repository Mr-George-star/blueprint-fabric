package net.george.blueprint.common.resource;

import com.google.common.base.Joiner;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines a resource pack from an arbitrary Path.
 * <p>
 * This is primarily intended to support including optional resource packs inside a mod,
 * such as to have alternative textures to use along with Programmer Art, or optional
 * alternative recipes for compatibility ot to replace vanilla recipes.
 */
public class PathResourcePack extends AbstractFileResourcePack {
    private final Path source;
    private final String packName;

    /**
     * Constructs a java.nio.Path-based resource pack.
     *
     * @param packName the identifying name of the pack.
     *                 This name should be unique within the pack finder, preferably the name of the file or folder containing the resources.
     * @param source the root path of the pack. This needs to point to the folder that contains "assets" and/or "data", not the asset folder itself!
     */
    public PathResourcePack(String packName, final Path source) {
        super(new File("dummy"));
        this.source = source;
        this.packName = packName;
    }

    /**
     * Returns the source path containing the resource pack.
     * This is used for error display.
     *
     * @return the root path of the resources.
     */
    public Path getSource() {
        return this.source;
    }

    /**
     * Returns the identifying name for the pack.
     *
     * @return the identifier of the pack.
     */
    @Override
    public String getName() {
        return this.packName;
    }

    /**
     * Implement to return a file or folder path for the given set of path components.
     * @param paths One or more path strings to resolve. Can include slash-separated paths.
     * @return the resulting path, which may not exist.
     */
    protected Path resolve(String... paths) {
        Path path = getSource();
        for (String name : paths)
            path = path.resolve(name);
        return path;
    }

    @Override
    protected InputStream openFile(String name) throws IOException {
        final Path path = resolve(name);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Can't find resource " + name + " at " + getSource());
        }
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Override
    protected boolean containsFile(String name) {
        final Path path = resolve(name);
        return Files.exists(path);
    }

    @SuppressWarnings("resource")
    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        try {
            Path root = resolve(type.getDirectory(), namespace).toAbsolutePath();
            Path inputPath = root.getFileSystem().getPath(prefix);

            return Files.walk(root)
                    .map(root::relativize)
                    .filter(path -> path.getNameCount() <= maxDepth && !path.toString().endsWith(".mcmeta") && path.startsWith(inputPath))
                    .filter(path -> pathFilter.test(path.getFileName().toString()))
                    // It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
                    // Join the path names ourselves to force forward slashes
                    .map(path -> new Identifier(namespace, Joiner.on('/').join(path)))
                    .collect(Collectors.toList());
        }
        catch (IOException exception) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public Set<String> getNamespaces(ResourceType type) {
        try {
            Path root = resolve(type.getDirectory());
            return Files.walk(root,1)
                    .map(root::relativize)
                    .filter(path -> path.getNameCount() > 0) // skip the root entry
                    .map(p->p.toString().replaceAll("/$","")) // remove the trailing slash, if present
                    .filter(s -> !s.isEmpty()) //filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
                    .collect(Collectors.toSet());
        }
        catch (IOException exception) {
            if (type == ResourceType.SERVER_DATA) { //We still have to add the resource namespace if client resources exist, as we load langs (which are in assets) on server
                return this.getNamespaces(ResourceType.CLIENT_RESOURCES);
            } else {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        if (id.getPath().startsWith("lang/")) {
            return super.open(ResourceType.CLIENT_RESOURCES, id);
        } else {
            return super.open(type, id);
        }
    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        if (id.getPath().startsWith("lang/")) {
            return super.contains(ResourceType.CLIENT_RESOURCES, id);
        } else {
            return super.contains(type, id);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getClass().getName(), getSource());
    }
}
