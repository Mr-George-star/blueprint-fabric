package net.george.blueprint.client.model.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for variant-type blockstates, i.e. non-multipart blockstates. Should
 * not be manually instantiated, instead use
 * {@link BlockStateProvider#getVariantBuilder(Block)}.
 * <p>
 * Variants can either be set via
 * {@link #setModels(PartialBlockstate, ConfiguredModel...)} or
 * {@link #addModels(PartialBlockstate, ConfiguredModel...)}, where model(s) can
 * be assigned directly to {@link PartialBlockstate partial states}, or builder
 * style via {@link #partialState()} and its subsequent methods.
 * <p>
 * This class also provides the convenience methods
 * {@link #forAllStates(Function)} and
 * {@link #forAllStatesExcept(Function, Property...)} for cases where the model
 * for each variant can be decided dynamically based on the state's property
 * values.
 *
 * @see BlockStateProvider
 */
@SuppressWarnings("unused")
public class VariantBlockStateBuilder implements IGeneratedBlockstate {
    private final Block owner;
    private final Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> models = new LinkedHashMap<>();
    private final Set<BlockState> coveredStates = new HashSet<>();

    VariantBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> getModels() {
        return this.models;
    }

    public Block getOwner() {
        return this.owner;
    }

    @Override
    public JsonObject toJson() {
        List<BlockState> missingStates = Lists.newArrayList(this.owner.getStateManager().getStates());
        missingStates.removeAll(this.coveredStates);
        Preconditions.checkState(missingStates.isEmpty(), "Blockstate for block %s does not cover all states. Missing: %s", this.owner, missingStates);
        JsonObject variants = new JsonObject();
        getModels().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(PartialBlockstate.comparingByProperties()))
                .forEach(entry -> variants.add(entry.getKey().toString(), entry.getValue().toJson()));
        JsonObject main = new JsonObject();
        main.add("variants", variants);
        return main;
    }

    /**
     * Assign some models to a given {@link PartialBlockstate partial state}.
     *
     * @param state  The {@link PartialBlockstate partial state} for which to add
     *               the models
     * @param models A set of models to add to this state
     * @return this builder
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws IllegalArgumentException if {@code models} is empty
     * @throws IllegalArgumentException if {@code state}'s owning block differs from
     *                                  the builder's
     * @throws IllegalArgumentException if {@code state} partially matches another
     *                                  state which has already been configured
     */
    public VariantBlockStateBuilder addModels(PartialBlockstate state, ConfiguredModel... models) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkArgument(models.length > 0, "Cannot set models to empty array");
        Preconditions.checkArgument(state.getOwner() == this.owner, "Cannot set models for a different block. Found: %s, Current: %s", state.getOwner(), this.owner);
        if (!this.models.containsKey(state)) {
            Preconditions.checkArgument(disjointToAll(state), "Cannot set models for a state for which a partial match has already been configured");
            this.models.put(state, new BlockStateProvider.ConfiguredModelList(models));
            for (BlockState fullState : this.owner.getStateManager().getStates()) {
                if (state.test(fullState)) {
                    this.coveredStates.add(fullState);
                }
            }
        } else {
            this.models.computeIfPresent(state, (blockState, list) -> list.append(models));
        }
        return this;
    }

    /**
     * Assign some models to a given {@link PartialBlockstate partial state},
     * throwing an exception if the state has already been configured. Otherwise,
     * simply calls {@link #addModels(PartialBlockstate, ConfiguredModel...)}.
     *
     * @param state  The {@link PartialBlockstate partial state} for which to set
     *               the models
     * @param models A set of models to assign to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} has already been configured
     * @see #addModels(PartialBlockstate, ConfiguredModel...)
     */
    public VariantBlockStateBuilder setModels(PartialBlockstate state, ConfiguredModel... models) {
        Preconditions.checkArgument(!this.models.containsKey(state), "Cannot set models for a state that has already been configured: %s", state);
        addModels(state, models);
        return this;
    }

    private boolean disjointToAll(PartialBlockstate newState) {
        return this.coveredStates.stream().noneMatch(newState);
    }

    public PartialBlockstate partialState() {
        return new PartialBlockstate(this.owner, this);
    }

    public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
        return forAllStatesExcept(mapper);
    }

    public VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper, Property<?>... ignored) {
        Set<PartialBlockstate> seen = new HashSet<>();
        for (BlockState fullState : this.owner.getStateManager().getStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getEntries());
            for (Property<?> property : ignored) {
                propertyValues.remove(property);
            }
            PartialBlockstate partialState = new PartialBlockstate(this.owner, propertyValues, this);
            if (seen.add(partialState)) {
                setModels(partialState, mapper.apply(fullState));
            }
        }
        return this;
    }

    public static class PartialBlockstate implements Predicate<BlockState> {
        private final Block owner;
        private final SortedMap<Property<?>, Comparable<?>> setStates;
        @Nullable
        private final VariantBlockStateBuilder outerBuilder;

        PartialBlockstate(Block owner, @Nullable VariantBlockStateBuilder outerBuilder) {
            this(owner, ImmutableMap.of(), outerBuilder);
        }

        PartialBlockstate(Block owner, Map<Property<?>, Comparable<?>> setStates, @Nullable VariantBlockStateBuilder outerBuilder) {
            this.owner = owner;
            this.outerBuilder = outerBuilder;
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                Property<?> property = entry.getKey();
                Comparable<?> value = entry.getValue();
                Preconditions.checkArgument(owner.getStateManager().getProperties().contains(property), "Property %s not found on block %s", entry, this.owner);
                Preconditions.checkArgument(property.getValues().contains(value), "%s is not a valid value for %s", value, property);
            }
            this.setStates = Maps.newTreeMap(Comparator.comparing(Property::getName));
            this.setStates.putAll(setStates);
        }

        public <T extends Comparable<T>> PartialBlockstate with(Property<T> property, T value) {
            Preconditions.checkArgument(!this.setStates.containsKey(property), "Property %s has already been set", property);
            Map<Property<?>, Comparable<?>> newState = new HashMap<>(this.setStates);
            newState.put(property, value);
            return new PartialBlockstate(this.owner, newState, this.outerBuilder);
        }

        private void checkValidOwner() {
            Preconditions.checkNotNull(this.outerBuilder, "Partial blockstate must have a valid owner to perform this action");
        }

        /**
         * Creates a builder for models to assign to this state, which when completed
         * via {@link ConfiguredModel.Builder#addModel()} will assign the resultant set
         * of models to this state.
         *
         * @return the model builder
         * @see ConfiguredModel.Builder
         */
        public ConfiguredModel.Builder<VariantBlockStateBuilder> modelForState() {
            checkValidOwner();
            return ConfiguredModel.builder(this.outerBuilder, this);
        }

        /**
         * Add models to the current state's variant. For use when it is more convenient
         * to add multiple sets of models, as a replacement for
         * {@link #setModels(ConfiguredModel...)}.
         *
         * @param models The models to add.
         * @return {@code this}
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public PartialBlockstate addModels(ConfiguredModel... models) {
            checkValidOwner();
            this.outerBuilder.addModels(this, models);
            return this;
        }

        /**
         * Set this variant's models, and return the parent builder.
         *
         * @param models The models to set
         * @return The parent builder instance
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
            checkValidOwner();
            return this.outerBuilder.setModels(this, models);
        }

        /**
         * Complete this state without adding any new models, and return a new partial
         * state via the parent builder. For use after calling
         * {@link #addModels(ConfiguredModel...)}.
         *
         * @return A fresh partial state as specified by
         *         {@link VariantBlockStateBuilder#partialState()}.
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public PartialBlockstate partialState() {
            checkValidOwner();
            return this.outerBuilder.partialState();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PartialBlockstate that = (PartialBlockstate)o;
            return this.owner.equals(that.owner) && this.setStates.equals(that.setStates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.owner, this.setStates);
        }

        public Block getOwner() {
            return this.owner;
        }

        public SortedMap<Property<?>, Comparable<?>> getSetStates() {
            return this.setStates;
        }

        @Override
        public boolean test(BlockState blockState) {
            if (blockState.getBlock() != getOwner()) {
                return false;
            }
            for (Map.Entry<Property<?>, Comparable<?>> entry : this.setStates.entrySet()) {
                if (blockState.get(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : this.setStates.entrySet()) {
                if (result.length() > 0) {
                    result.append(',');
                }
                result.append(entry.getKey().getName())
                        .append('=')
                        .append(((Property)entry.getKey()).name(entry.getValue()));
            }
            return result.toString();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static Comparator<PartialBlockstate> comparingByProperties() {
            return (s1, s2) -> {
                SortedSet<Property<?>> propertyUniverse = new TreeSet<>(s1.getSetStates().comparator().reversed());
                propertyUniverse.addAll(s1.getSetStates().keySet());
                propertyUniverse.addAll(s2.getSetStates().keySet());
                for (Property<?> property : propertyUniverse) {
                    Comparable val1 = s1.getSetStates().get(property);
                    Comparable val2 = s2.getSetStates().get(property);
                    if (val1 == null && val2 != null) {
                        return -1;
                    } else if (val2 == null && val1 != null) {
                        return 1;
                    } else if (val1 != null){
                        int compared = val1.compareTo(val2);
                        if (compared != 0) {
                            return compared;
                        }
                    }
                }
                return 0;
            };
        }
    }
}
