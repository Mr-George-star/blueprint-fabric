package net.george.blueprint.core.api.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.io.Serial;
import java.util.*;

@SuppressWarnings("unused")
public class BiomeManager {
    private static final TrackedList<BiomeEntry>[] biomes = setupBiomes();
    private static final List<RegistryKey<Biome>> additionalOverworldBiomes = new ArrayList<>();
    private static final List<RegistryKey<Biome>> additionalOverworldBiomesView = Collections.unmodifiableList(additionalOverworldBiomes);

    private static TrackedList<BiomeEntry>[] setupBiomes() {
        @SuppressWarnings("unchecked")
        TrackedList<BiomeEntry>[] currentBiomes = new TrackedList[BiomeType.values().length];

        currentBiomes[BiomeType.DESERT_LEGACY.ordinal()] = new TrackedList<>(
                new BiomeEntry(BiomeKeys.DESERT, 10),
                new BiomeEntry(BiomeKeys.FOREST, 10),
                new BiomeEntry(BiomeKeys.SWAMP, 10),
                new BiomeEntry(BiomeKeys.PLAINS, 10),
                new BiomeEntry(BiomeKeys.TAIGA, 10)
        );

        currentBiomes[BiomeType.DESERT.ordinal()] = new TrackedList<>(
                new BiomeEntry(BiomeKeys.DESERT, 30),
                new BiomeEntry(BiomeKeys.SAVANNA, 20),
                new BiomeEntry(BiomeKeys.PLAINS, 10)
        );

        currentBiomes[BiomeType.WARM.ordinal()] = new TrackedList<>(
                new BiomeEntry(BiomeKeys.FOREST, 10),
                new BiomeEntry(BiomeKeys.DARK_FOREST, 10),
                new BiomeEntry(BiomeKeys.PLAINS, 10),
                new BiomeEntry(BiomeKeys.BIRCH_FOREST, 10),
                new BiomeEntry(BiomeKeys.SWAMP, 10)
        );

        currentBiomes[BiomeType.COOL.ordinal()] = new TrackedList<>(
                new BiomeEntry(BiomeKeys.FOREST, 10),
                new BiomeEntry(BiomeKeys.TAIGA, 10),
                new BiomeEntry(BiomeKeys.PLAINS, 10)
        );

        currentBiomes[BiomeType.ICY.ordinal()] = new TrackedList<>(
                new BiomeEntry(BiomeKeys.SNOWY_TAIGA, 10)
        );

        return currentBiomes;
    }

    /**
     * Add biomes that you add to the overworld without using {@link BiomeManager#addBiome(BiomeType, BiomeEntry)}
     */
    public static void addAdditionalOverworldBiomes(RegistryKey<Biome> biome) {
        if (!"minecraft".equals(biome.getValue().getNamespace()) && additionalOverworldBiomes.stream().noneMatch(entry -> entry.getValue().equals(biome.getValue()))) {
            additionalOverworldBiomes.add(biome);
        }
    }

    public static boolean addBiome(BiomeType type, BiomeEntry entry) {
        int index = type.ordinal();
        List<BiomeEntry> list = index > biomes.length ? null : biomes[index];
        if (list != null) {
            additionalOverworldBiomes.add(entry.key);
            return list.add(entry);
        }
        return false;
    }

    public static boolean removeBiome(BiomeType type, BiomeEntry entry) {
        int index = type.ordinal();
        List<BiomeEntry> list = index > biomes.length ? null : biomes[index];
        return list != null && list.remove(entry);
    }

    /**
     * @return list of biomes that might be generated in the overworld in addition to the vanilla biomes
     */
    public static List<RegistryKey<Biome>> getAdditionalOverworldBiomes() {
        return additionalOverworldBiomesView;
    }

    public static ImmutableList<BiomeEntry> getBiomes(BiomeType type) {
        int index = type.ordinal();
        List<BiomeEntry> list = index >= biomes.length ? null : biomes[index];
        return list != null ? ImmutableList.copyOf(list) : ImmutableList.of();
    }

    public static boolean isTypeListModded(BiomeType type) {
        int index = type.ordinal();
        TrackedList<BiomeEntry> list = index > biomes.length ? null : biomes[index];
        return list != null && list.isModded();
    }

    public enum BiomeType {
        DESERT,
        DESERT_LEGACY,
        WARM,
        COOL,
        ICY
    }

    public static class BiomeEntry {
        private final RegistryKey<Biome> key;

        public BiomeEntry(RegistryKey<Biome> key, int weight) {
            this.key = key;
        }

        public RegistryKey<Biome> getKey() {
            return this.key;
        }
    }

    private static class TrackedList<E> extends ArrayList<E> {
        @Serial
        private static final long serialVersionUID = 1L;
        private boolean isModded = false;

        @SafeVarargs
        private <T extends E> TrackedList(T... contents) {
            super(Arrays.asList(contents));
        }

        @Override
        public E set(int index, E element) {
            this.isModded = true;
            return super.set(index, element);
        }

        @Override
        public boolean add(E element) {
            this.isModded = true;
            return super.add(element);
        }

        @Override
        public void add(int index, E element) {
            this.isModded = true;
            super.add(index, element);
        }

        @Override
        public E remove(int index) {
            this.isModded = true;
            return super.remove(index);
        }

        @Override
        public boolean remove(Object obj) {
            this.isModded = true;
            return super.remove(obj);
        }

        @Override
        public void clear() {
            this.isModded = true;
            super.clear();
        }

        @Override
        public boolean addAll(Collection<? extends E> collection) {
            this.isModded = true;
            return super.addAll(collection);
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> collection) {
            this.isModded = true;
            return super.addAll(index, collection);
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            this.isModded = true;
            return super.removeAll(collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            this.isModded = true;
            return super.retainAll(collection);
        }

        public boolean isModded() {
            return this.isModded;
        }
    }
}
