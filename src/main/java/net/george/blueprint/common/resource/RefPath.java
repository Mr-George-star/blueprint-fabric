package net.george.blueprint.common.resource;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class RefPath implements Path {
    private final FileSystem fileSystem;
    private final RefPath root;
    private final boolean absolute;
    private final String[] pathParts;
    private final String key;
    private RefPath normalized;

    public RefPath(final FileSystem fileSystem, String key, final String... pathParts) {
        this.fileSystem = fileSystem;
        this.key = key;
        this.root = new RefPath(fileSystem, "/");
        if (pathParts.length == 0) {
            this.absolute = false;
            this.pathParts = new String[0];
        } else {
            final String longString = Arrays.stream(pathParts).filter(part -> !part.isEmpty()).collect(Collectors.joining(this.getFileSystem().getSeparator()));
            this.absolute = longString.startsWith(this.getFileSystem().getSeparator());
            this.pathParts = getPathParts(longString);
        }
        this.normalized = null;
    }

    private RefPath(final FileSystem fileSystem, String key, boolean absolute, boolean isNormalized, final String... pathParts) {
        this.fileSystem = fileSystem;
        this.key = key;
        this.root = new RefPath(fileSystem, "/");
        this.absolute = absolute;
        this.pathParts = pathParts;
        if (isNormalized)
            this.normalized = this;
        else
            this.normalized = null;
    }

    public RefPath(FileSystem fileSystem, String key, boolean absolute, String... pathParts) {
        this.fileSystem = fileSystem;
        this.key = key;
        this.root = new RefPath(fileSystem, "/");
        this.absolute = absolute;
        this.pathParts = pathParts;
    }
    private String[] getPathParts(final String longString) {
        String separated = "(?:" + Pattern.quote(this.getFileSystem().getSeparator()) + ")";
        String pathName = longString
                .replace("\\", this.getFileSystem().getSeparator())
                // remove separators from start and end of long string
                .replaceAll("^" + separated + "*|" + separated + "*$", "")
                // Remove duplicate separators
                .replaceAll(separated + "+(?=" + separated + ")", "");
        if (pathName.isEmpty()) {
            return new String[0];
        } else {
            return pathName.split(this.getFileSystem().getSeparator());
        }
    }


    @NotNull
    @Override
    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return this.absolute;
    }

    @Override
    public Path getRoot() {
        // Found nothing in the docs that say a non-absolute path can't have a root
        // although this is uncommon. However, other stuff relies on it so leave it
        //if (!this.absolute)
        //    return null;
        return this.root;
    }

    @Override
    public Path getFileName() {
        if (this.pathParts.length > 0) {
            return new RefPath(this.getFileSystem(), this.key, false, this.pathParts[this.pathParts.length - 1]);
        } else {
            // normally would be null for the empty absolute path and empty string for the empty relative
            // path. But again, very much stuff relies on it and there's no current directory for union
            // paths, so it does not really matter.
            return new RefPath(this.fileSystem, this.key, false);
        }
    }

    @Override
    public Path getParent() {
        if (this.pathParts.length > 0) {
            return new RefPath(this.fileSystem, this.key, this.absolute, Arrays.copyOf(this.pathParts,this.pathParts.length - 1));
        } else {
            return null;
        }
    }

    @Override
    public int getNameCount() {
        return this.pathParts.length;
    }

    @NotNull
    @Override
    public Path getName(int index) {
        if (index < 0 || index > this.pathParts.length -1) {
            throw new IllegalArgumentException();
        }
        return new RefPath(this.fileSystem, this.key, false, this.pathParts[index]);
    }

    @NotNull
    @Override
    public RefPath subpath(int beginIndex, int endIndex) {
        if (!this.absolute && this.pathParts.length == 0 && beginIndex == 0 && endIndex == 1)
            return new RefPath(this.fileSystem, this.key, false);
        if (beginIndex < 0 || beginIndex > this.pathParts.length - 1 || endIndex < 0 || endIndex > this.pathParts.length || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Out of range "+beginIndex+" to "+endIndex+" for length "+this.pathParts.length);
        }
        if (!this.absolute && beginIndex == 0 && endIndex == this.pathParts.length) {
            return this;
        }
        return new RefPath(this.fileSystem, this.key, false, Arrays.copyOfRange(this.pathParts, beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        if (other.getFileSystem() != this.getFileSystem()) {
            return false;
        }
        if (other instanceof RefPath subPath) {
            if (this.absolute != subPath.absolute)
                return false;
            return checkArraysMatch(this.pathParts, subPath.pathParts, false);
        }
        return false;
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        if (other.getFileSystem() != this.getFileSystem()) {
            return false;
        }
        if (other instanceof RefPath bp) {
            if (!this.absolute && bp.absolute)
                return false;
            return checkArraysMatch(this.pathParts, bp.pathParts, true);
        }
        return false;
    }

    private static boolean checkArraysMatch(String[] array1, String[] array2, boolean reverse) {
        int length = Math.min(array1.length, array2.length);
        IntBinaryOperator offset = reverse ? (l, i) -> l - i - 1 : (l, i) -> i;
        for (int i = 0; i < length; i++) {
            if (!Objects.equals(array1[offset.applyAsInt(array1.length, i)], array2[offset.applyAsInt(array2.length, i)]))
                return false;
        }
        return true;
    }

    @NotNull
    @Override
    public Path normalize() {
        if (this.normalized != null)
            return this.normalized;
        Deque<String> normpath = new ArrayDeque<>();
        for (String pathPart : this.pathParts) {
            switch (pathPart) {
                case ".":
                    break;
                case "..":
                    if (normpath.isEmpty() || normpath.getLast().equals("..")) {
                        // ... on an empty path is allowed, so keep it
                        normpath.addLast(pathPart);
                    } else {
                        normpath.removeLast();
                    }
                    break;
                default:
                    normpath.addLast(pathPart);
                    break;
            }
        }
        this.normalized = new RefPath(this.fileSystem, this.key, this.absolute, true, normpath.toArray(new String[0]));
        return this.normalized;
    }

    @NotNull
    @Override
    public Path resolve(@NotNull Path other) {
        if (other instanceof RefPath path) {
            if (path.isAbsolute()) {
                return path;
            }
            String[] mergedParts = new String[this.pathParts.length + path.pathParts.length];
            System.arraycopy(this.pathParts, 0, mergedParts, 0, this.pathParts.length);
            System.arraycopy(path.pathParts, 0, mergedParts, this.pathParts.length, path.pathParts.length);
            return new RefPath(this.fileSystem, this.key, this.absolute, mergedParts);
        }
        return other;
    }

    @NotNull
    @Override
    public Path relativize(@NotNull Path other) {
        if (other.getFileSystem()!=this.getFileSystem()) throw new IllegalArgumentException("Wrong filesystem");
        if (other instanceof RefPath path) {
            if (this.absolute != path.absolute) {
                // Should not be allowed but union fs relies on it
                // also there is no such concept of a current directory for union paths
                // meaning absolute and relative paths should have the same effect,
                // so we just allow this.
                //throw new IllegalArgumentException("Different types of path");
            }
            int length = Math.min(this.pathParts.length, path.pathParts.length);
            int i = 0;
            while (i < length) {
                if (!Objects.equals(this.pathParts[i], path.pathParts[i]))
                    break;
                i++;
            }

            int remaining = this.pathParts.length - i;
            if (remaining == 0 && i == path.pathParts.length) {
                return new RefPath(this.getFileSystem(), this.key, false);
            } else if (remaining == 0) {
                return path.subpath(i, path.getNameCount());
            } else {
                String[] updots = IntStream.range(0, remaining).mapToObj(idx -> "..").toArray(String[]::new);
                if (i == path.pathParts.length) {
                    return new RefPath(this.getFileSystem(), this.key, false, updots);
                } else {
                    RefPath subPath = path.subpath(i, path.getNameCount());
                    String[] mergedParts = new String[updots.length + subPath.pathParts.length];
                    System.arraycopy(updots, 0, mergedParts, 0, updots.length);
                    System.arraycopy(subPath.pathParts, 0, mergedParts, updots.length, subPath.pathParts.length);
                    return new RefPath(this.getFileSystem(), this.key, false, mergedParts);
                }
            }
        }
        throw new IllegalArgumentException("Wrong filesystem");
    }

    @NotNull
    @Override
    public URI toUri() {
        try {
            return new URI(
                    this.fileSystem.provider().getScheme(),
                    null,
                    this.key + '!' + toAbsolutePath(),
                    null
            );
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }

    @NotNull
    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }else {
            return this.root.resolve(this);
        }
    }

    @NotNull
    @Override
    public Path toRealPath(@NotNull LinkOption @NotNull ... options) {
        return this.toAbsolutePath().normalize();
    }

    @NotNull
    @Override
    public WatchKey register(@NotNull WatchService watcher, @NotNull WatchEvent.Kind<?> @NotNull [] events, WatchEvent.Modifier... modifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(final @NotNull Path other) {
        if (other instanceof RefPath path) {
            if (this.absolute && !path.absolute) {
                return 1;
            } else if (!this.absolute && path.absolute) {
                return -1;
            } else {
                return Arrays.compare(this.pathParts, path.pathParts);
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof RefPath path) {
            return path.getFileSystem() == this.getFileSystem() && this.absolute == path.absolute && Arrays.equals(this.pathParts, path.pathParts);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.fileSystem) + 31 * Arrays.hashCode(this.pathParts);
    }

    @Override
    public String toString() {
        return (this.absolute ? this.fileSystem.getSeparator() : "") + String.join(this.fileSystem.getSeparator(), this.pathParts);
    }
}
