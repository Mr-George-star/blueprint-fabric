package net.george.blueprint.core.api.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class BiomeDictionary {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<RegistryKey<Biome>, BiomeInfo> biomeInfoMap = new HashMap<>();

    public static final class Type {
        private static final Map<String, Type> byName = new TreeMap<>();
        private static final Collection<Type> allTypes = Collections.unmodifiableCollection(byName.values());

        /*
         * Temperature-based tags. Specifying neither implies a biome is temperate
         */
        public static final Type HOT = new Type("HOT");
        public static final Type COLD = new Type("COLD");

        /*
         * Tags specifying the amount of vegetation a biome has. Specifying neither implies a biome to have moderate amounts
         */
        public static final Type SPARSE = new Type("SPARSE");
        public static final Type DENSE = new Type("DENSE");

        /*
         * Tags specifying how moist a biome is. Specifying neither implies the biome as having moderate humidity
         */
        public static final Type WET = new Type("WET");
        public static final Type DRY = new Type("DRY");

        /*
         * Tree-based tags, SAVANNA refers to dry, desert-like trees (Such as Acacia),
         * CONIFEROUS refers to snowy trees (Such as Spruce) and JUNGLE refers to jungle trees.
         * Specifying no tag implies a biome has temperate trees (Such as Oak)
         */
        public static final Type SAVANNA = new Type("SAVANNA");
        public static final Type CONIFEROUS = new Type("CONIFEROUS");
        public static final Type JUNGLE = new Type("JUNGLE");

        /*
         * Tags specifying the nature of a biome
         */
        public static final Type SPOOKY = new Type("SPOOKY");
        public static final Type DEAD = new Type("DEAD");
        public static final Type LUSH = new Type("LUSH");
        public static final Type MUSHROOM = new Type("MUSHROOM");
        public static final Type MAGICAL = new Type("MAGICAL");
        public static final Type RARE = new Type("RARE");
        public static final Type PLATEAU = new Type("PLATEAU");
        public static final Type MODIFIED = new Type("MODIFIED");

        public static final Type OCEAN = new Type("OCEAN");
        public static final Type RIVER = new Type("RIVER");

        /*
         * A general tag for all water-based BiomeKeys. Shown as present if OCEAN or RIVER are.
         **/
        public static final Type WATER = new Type("WATER", OCEAN, RIVER);

        /*
         * Generic types which a biome can be
         */
        public static final Type MESA = new Type("MESA");
        public static final Type FOREST = new Type("FOREST");
        public static final Type PLAINS = new Type("PLAINS");
        public static final Type HILLS = new Type("HILLS");
        public static final Type SWAMP = new Type("SWAMP");
        public static final Type SANDY = new Type("SANDY");
        public static final Type SNOWY = new Type("SNOWY");
        public static final Type WASTELAND = new Type("WASTELAND");
        public static final Type BEACH = new Type("BEACH");
        public static final Type VOID = new Type("VOID");
        public static final Type UNDERGROUND = new Type("UNDERGROUND");

        /*
         * Mountain related tags
         */
        public static final Type PEAK = new Type("PEAK");
        public static final Type SLOPE = new Type("SLOPE");
        public static final Type MOUNTAIN = new Type("MOUNTAIN", PEAK, SLOPE);

        /*
         * Tags specifying the dimension a biome generates in. Specifying none implies a biome that generates in a modded dimension
         */
        public static final Type OVERWORLD = new Type("OVERWORLD");
        public static final Type NETHER = new Type("NETHER");
        public static final Type END = new Type("END");

        private final String name;
        private final List<Type> subTypes;
        private final Set<RegistryKey<Biome>> biomeKeys = new HashSet<>();
        private final Set<RegistryKey<Biome>> biomeKeysUnmodifiable = Collections.unmodifiableSet(biomeKeys);

        private Type(String name, Type... subTypes) {
            this.name = name;
            this.subTypes = ImmutableList.copyOf(subTypes);

            byName.put(name, this);
        }

        /**
         * Gets the name for this type.
         */
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        /**
         * Retrieves a Type instance by name,
         * if one does not exist already it creates one.
         * This can be used as intermediate measure for modders to
         * add their own Biome types.
         * <p>
         * There are <i>no</i> naming conventions besides:
         * <ul><li><b>Must</b> be all upper case (enforced by name.toUpper())</li>
         * <li><b>No</b> Special characters. {Unenforced, just don't be a pain, if it becomes a issue I WILL
         * make this RTE with no worry about backwards compatibility}</li></ul>
         * <p>
         * Note: For performance’s sake, the return value of this function SHOULD be cached.
         * Two calls with the same name SHOULD return the same value.
         *
         * @param name The name of this Type
         * @return An instance of Type for this name.
         */
        public static Type getType(String name, Type... subTypes) {
            name = name.toUpperCase();
            Type t = byName.get(name);
            if (t == null) {
                t = new Type(name, subTypes);
            }
            return t;
        }

        /**
         * Checks if a type instance exists for a given name. Does not have any side effects if a type does not already exist.
         * This can be used for checking if a user-defined type is valid, for example, in a codec which accepts biome dictionary names.
         *
         * @param name The name.
         * @return {@code true} if a type exists with this name.
         * @see #getType(String, Type...) #getType for type naming conventions.
         */
        public static boolean hasType(String name) {
            return byName.containsKey(name.toUpperCase());
        }

        /**
         * @return An unmodifiable collection of all current biome types.
         */
        public static Collection<Type> getAll() {
            return allTypes;
        }

        @Nullable
        public static Type fromVanilla(Biome.Category category) {
            if (category == Biome.Category.NONE) {
                return null;
            }
            if (category == Biome.Category.THEEND) {
                return VOID;
            }
            return getType(category.name());
        }
    }

    private static class BiomeInfo {
        private final Set<Type> types = new HashSet<>();
        private final Set<Type> typesUnmodifiable = Collections.unmodifiableSet(this.types);
    }

    public static void init() {
    }

    static {
        registerVanillaBiomeKeys();
    }

    /**
     * Adds the given types to the biome.
     */
    public static void addTypes(RegistryKey<Biome> biome, Type... types) {
        Collection<Type> supertypes = listSupertypes(types);
        Collections.addAll(supertypes, types);

        for (Type type : supertypes) {
            type.biomeKeys.add(biome);
        }

        BiomeInfo biomeInfo = getBiomeInfo(biome);
        Collections.addAll(biomeInfo.types, types);
        biomeInfo.types.addAll(supertypes);
    }

    /**
     * Gets the set of BiomeKeys that have the given type.
     */
    @NotNull
    public static Set<RegistryKey<Biome>> getBiomeKeys(Type type) {
        return type.biomeKeysUnmodifiable;
    }

    /**
     * Gets the set of types that have been added to the given biome.
     */
    @NotNull
    public static Set<Type> getTypes(RegistryKey<Biome> biome) {
        return getBiomeInfo(biome).typesUnmodifiable;
    }

    /**
     * Checks if the two given BiomeKeys have types in common.
     *
     * @return returns true if a common type is found, false otherwise
     */
    public static boolean areSimilar(RegistryKey<Biome> biomeA, RegistryKey<Biome> biomeB) {
        Set<Type> typesA = getTypes(biomeA);
        Set<Type> typesB = getTypes(biomeB);
        return typesA.stream().anyMatch(typesB::contains);
    }

    /**
     * Checks if the given type has been added to the given biome.
     */
    public static boolean hasType(RegistryKey<Biome> biome, Type type) {
        return getTypes(biome).contains(type);
    }

    /**
     * Checks if any type has been added to the given biome.
     */
    public static boolean hasAnyType(RegistryKey<Biome> biome) {
        return !getBiomeInfo(biome).types.isEmpty();
    }

    //Internal implementation
    private static BiomeInfo getBiomeInfo(RegistryKey<Biome> biome) {
        return biomeInfoMap.computeIfAbsent(biome, k -> new BiomeInfo());
    }

    private static Collection<Type> listSupertypes(Type... types) {
        Set<Type> supertypes = new HashSet<>();
        Deque<Type> next = new ArrayDeque<>();
        Collections.addAll(next, types);

        while (!next.isEmpty()) {
            Type type = next.remove();

            for (Type sType : Type.byName.values()) {
                if (sType.subTypes.contains(type) && supertypes.add(sType))
                    next.add(sType);
            }
        }

        return supertypes;
    }

    private static void registerVanillaBiomeKeys() {
        addTypes(BiomeKeys.OCEAN, Type.OCEAN, Type.OVERWORLD);
        addTypes(BiomeKeys.PLAINS, Type.PLAINS, Type.OVERWORLD);
        addTypes(BiomeKeys.DESERT, Type.HOT, Type.DRY, Type.SANDY, Type.OVERWORLD);
        addTypes(BiomeKeys.WINDSWEPT_HILLS, Type.HILLS, Type.OVERWORLD);
        addTypes(BiomeKeys.FOREST, Type.FOREST, Type.OVERWORLD);
        addTypes(BiomeKeys.TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.OVERWORLD);
        addTypes(BiomeKeys.SWAMP, Type.WET, Type.SWAMP, Type.OVERWORLD);
        addTypes(BiomeKeys.RIVER, Type.RIVER, Type.OVERWORLD);
        addTypes(BiomeKeys.NETHER_WASTES, Type.HOT, Type.DRY, Type.NETHER);
        addTypes(BiomeKeys.THE_END, Type.COLD, Type.DRY, Type.END);
        addTypes(BiomeKeys.FROZEN_OCEAN, Type.COLD, Type.OCEAN, Type.SNOWY, Type.OVERWORLD);
        addTypes(BiomeKeys.FROZEN_RIVER, Type.COLD, Type.RIVER, Type.SNOWY, Type.OVERWORLD);
        addTypes(BiomeKeys.SNOWY_PLAINS, Type.COLD, Type.SNOWY, Type.WASTELAND, Type.OVERWORLD);
        addTypes(BiomeKeys.MUSHROOM_FIELDS, Type.MUSHROOM, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.BEACH, Type.BEACH, Type.OVERWORLD);
        addTypes(BiomeKeys.JUNGLE, Type.HOT, Type.WET, Type.DENSE, Type.JUNGLE, Type.OVERWORLD);
        addTypes(BiomeKeys.SPARSE_JUNGLE, Type.HOT, Type.WET, Type.JUNGLE, Type.FOREST, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.DEEP_OCEAN, Type.OCEAN, Type.OVERWORLD);
        addTypes(BiomeKeys.STONY_SHORE, Type.BEACH, Type.OVERWORLD);
        addTypes(BiomeKeys.SNOWY_BEACH, Type.COLD, Type.BEACH, Type.SNOWY, Type.OVERWORLD);
        addTypes(BiomeKeys.BIRCH_FOREST, Type.FOREST, Type.OVERWORLD);
        addTypes(BiomeKeys.DARK_FOREST, Type.SPOOKY, Type.DENSE, Type.FOREST, Type.OVERWORLD);
        addTypes(BiomeKeys.SNOWY_TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.SNOWY, Type.OVERWORLD);
        addTypes(BiomeKeys.OLD_GROWTH_PINE_TAIGA, Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.OVERWORLD);
        addTypes(BiomeKeys.WINDSWEPT_FOREST, Type.HILLS, Type.FOREST, Type.SPARSE, Type.OVERWORLD);
        addTypes(BiomeKeys.SAVANNA, Type.HOT, Type.SAVANNA, Type.PLAINS, Type.SPARSE, Type.OVERWORLD);
        addTypes(BiomeKeys.SAVANNA_PLATEAU, Type.HOT, Type.SAVANNA, Type.PLAINS, Type.SPARSE, Type.RARE, Type.OVERWORLD, Type.SLOPE, Type.PLATEAU);
        addTypes(BiomeKeys.BADLANDS, Type.MESA, Type.SANDY, Type.DRY, Type.OVERWORLD);
        addTypes(BiomeKeys.WOODED_BADLANDS, Type.MESA, Type.SANDY, Type.DRY, Type.SPARSE, Type.OVERWORLD, Type.SLOPE, Type.PLATEAU);
        addTypes(BiomeKeys.MEADOW, Type.PLAINS, Type.PLATEAU, Type.SLOPE, Type.OVERWORLD);
        addTypes(BiomeKeys.GROVE, Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.SNOWY, Type.SLOPE, Type.OVERWORLD);
        addTypes(BiomeKeys.SNOWY_SLOPES, Type.COLD, Type.SPARSE, Type.SNOWY, Type.SLOPE, Type.OVERWORLD);
        addTypes(BiomeKeys.JAGGED_PEAKS, Type.COLD, Type.SPARSE, Type.SNOWY, Type.PEAK, Type.OVERWORLD);
        addTypes(BiomeKeys.FROZEN_PEAKS, Type.COLD, Type.SPARSE, Type.SNOWY, Type.PEAK, Type.OVERWORLD);
        addTypes(BiomeKeys.STONY_PEAKS, Type.HOT, Type.PEAK, Type.OVERWORLD);
        addTypes(BiomeKeys.SMALL_END_ISLANDS, Type.END);
        addTypes(BiomeKeys.END_MIDLANDS, Type.END);
        addTypes(BiomeKeys.END_HIGHLANDS, Type.END);
        addTypes(BiomeKeys.END_BARRENS, Type.END);
        addTypes(BiomeKeys.WARM_OCEAN, Type.OCEAN, Type.HOT, Type.OVERWORLD);
        addTypes(BiomeKeys.LUKEWARM_OCEAN, Type.OCEAN, Type.OVERWORLD);
        addTypes(BiomeKeys.COLD_OCEAN, Type.OCEAN, Type.COLD, Type.OVERWORLD);
        addTypes(BiomeKeys.DEEP_LUKEWARM_OCEAN, Type.OCEAN, Type.OVERWORLD);
        addTypes(BiomeKeys.DEEP_COLD_OCEAN, Type.OCEAN, Type.COLD, Type.OVERWORLD);
        addTypes(BiomeKeys.DEEP_FROZEN_OCEAN, Type.OCEAN, Type.COLD, Type.OVERWORLD);
        addTypes(BiomeKeys.THE_VOID, Type.VOID);
        addTypes(BiomeKeys.SUNFLOWER_PLAINS, Type.PLAINS, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, Type.HILLS, Type.SPARSE, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.FLOWER_FOREST, Type.FOREST, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.ICE_SPIKES, Type.COLD, Type.SNOWY, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, Type.FOREST, Type.DENSE, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, Type.DENSE, Type.FOREST, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.WINDSWEPT_SAVANNA, Type.HOT, Type.DRY, Type.SPARSE, Type.SAVANNA, Type.HILLS, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.ERODED_BADLANDS, Type.MESA, Type.HOT, Type.DRY, Type.SPARSE, Type.RARE, Type.OVERWORLD);
        addTypes(BiomeKeys.BAMBOO_JUNGLE, Type.HOT, Type.WET, Type.RARE, Type.JUNGLE, Type.OVERWORLD);
        addTypes(BiomeKeys.LUSH_CAVES, Type.UNDERGROUND, Type.LUSH, Type.WET, Type.OVERWORLD);
        addTypes(BiomeKeys.DRIPSTONE_CAVES, Type.UNDERGROUND, Type.SPARSE, Type.OVERWORLD);
        addTypes(BiomeKeys.SOUL_SAND_VALLEY, Type.HOT, Type.DRY, Type.NETHER);
        addTypes(BiomeKeys.CRIMSON_FOREST, Type.HOT, Type.DRY, Type.NETHER, Type.FOREST);
        addTypes(BiomeKeys.WARPED_FOREST, Type.HOT, Type.DRY, Type.NETHER, Type.FOREST);
        addTypes(BiomeKeys.BASALT_DELTAS, Type.HOT, Type.DRY, Type.NETHER);

        if (DEBUG) { 
            StringBuilder buf = new StringBuilder();
            buf.append("BiomeDictionary:\n");
            Type.byName.forEach((name, type) -> buf.append("    ").append(type.name).append(": ")
                            .append(type.biomeKeys.stream()
                                    .map(RegistryKey::getValue)
                                    .sorted(Identifier::compareNamespace)
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", "))).append('\n')
            );

            boolean missing = false;
            List<RegistryKey<Biome>> all = StreamSupport.stream(BuiltinRegistries.BIOME.spliterator(), false)
                    .map(biome -> RegistryKey.of(Registry.BIOME_KEY, BuiltinRegistries.BIOME.getId(biome)))
                    .sorted().toList();

            for (RegistryKey<Biome> key : all) {
                if (!biomeInfoMap.containsKey(key)) {
                    if (!missing) {
                        buf.append("Missing:\n");
                        missing = true;
                    }
                    buf.append("    ").append(key.getValue()).append('\n');
                }
            }
            LOGGER.debug(buf.toString());
        }
    }
}
