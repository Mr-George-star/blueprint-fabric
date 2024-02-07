package net.george.blueprint.common.resource;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ResourcePackLoader {
    private static Map<ModContainer, PathResourcePack> modResourcePacks;
    private static int index = 0;

    public ResourcePackLoader() {
    }

    public static Optional<PathResourcePack> getPackFor(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(modContainer -> modResourcePacks.get(modContainer));
    }

    /** @deprecated */
    @Deprecated
    public static void loadResourcePacks(ResourcePackManager resourcePacks, BiFunction<Map<ModContainer, ? extends PathResourcePack>, BiConsumer<? super PathResourcePack, ResourcePackProfile>, ? extends ResourcePackProvider> packFinder) {
        loadResourcePacks(resourcePacks, (map) -> packFinder.apply(map, (pack, profile) -> {}));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void loadResourcePacks(ResourcePackManager resourcePacks, Function<Map<ModContainer, ? extends PathResourcePack>, ? extends ResourcePackProvider> packFinder) {
        modResourcePacks = (Map)FabricLoader.getInstance().getAllMods().stream().map((mod) ->
                Pair.of(mod, createPackForMod(mod))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (u, v) -> {
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Duplicate key %s", u));
        }, LinkedHashMap::new));
        resourcePacks.providers.add(packFinder.apply(modResourcePacks));
    }

    @SuppressWarnings("deprecation")
    public static @NotNull ResourcePack createPackForMod(final ModContainer mod) {
        return new PathResourcePack(mod.getMetadata().getName(), mod.getRootPath()) {
            protected @NotNull Path resolve(String... paths) {
                if (paths.length < 1) {
                    throw new IllegalArgumentException("Missing path");
                } else {
                    String path = String.join("/", paths);
                    Iterator<ModContainer> iterator = FabricLoader.getInstance().getAllMods().iterator();

                    ModContainer modContainer;
                    do {
                        if (!iterator.hasNext()) {
                            return mod.findPath(path).orElse(new RefPath(mod.getRootPath().getFileSystem(), ResourcePackLoader.makeKey(mod.getRootPath().getRoot()), path));
                        }

                        modContainer = iterator.next();
                    } while (modContainer.findPath(path).isEmpty());

                    return modContainer.findPath(path).get();
                }
            }
        };
    }

    private static synchronized String makeKey(Path path) {
        String key = path.toAbsolutePath().normalize().toUri().getPath();
        String replaced = key.replace('!', '_');
        return replaced + "#" + index++;
    }

    public static List<String> getPackNames() {
        return FabricLoader.getInstance().getAllMods().stream().map((mod) -> "mod:" + mod.getMetadata().getId())
                .filter((name) -> !name.equals("mod:minecraft")).collect(Collectors.toList());
    }

    public static <V> Comparator<Map.Entry<String, V>> getSorter() {
        List<String> order = new ArrayList<>();
        order.add("vanilla");
        order.add("mod_resources");
        Stream<String> allMods = FabricLoader.getInstance().getAllMods().stream().map((mod) -> mod.getMetadata().getId()).map((mod) -> "mod:" + mod);
        Objects.requireNonNull(order);
        allMods.forEach(order::add);
        Object2IntMap<String> order_f = new Object2IntOpenHashMap<>(order.size());

        for(int x = 0; x < order.size(); ++x) {
            order_f.put(order.get(x), x);
        }

        return (e1, e2) -> {
            String s1 = e1.getKey();
            String s2 = e2.getKey();
            int i1 = order_f.getOrDefault(s1, -1);
            int i2 = order_f.getOrDefault(s2, -1);
            if (i1 == i2 && i1 == -1) {
                return s1.compareTo(s2);
            } else if (i1 == -1) {
                return 1;
            } else {
                return i2 == -1 ? -1 : i2 - i1;
            }
        };
    }
}
