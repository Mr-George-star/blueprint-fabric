package net.george.blueprint.common.advancement;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * A {@link Criterion} implementation designed for advancements that can only be triggered in code.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class EmptyTrigger implements Criterion<EmptyTrigger.Instance> {
    private final Map<PlayerAdvancementTracker, Listeners> listeners = Maps.newHashMap();
    private final Identifier id;

    public EmptyTrigger(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer<Instance> conditions) {
        Listeners listeners = this.listeners.computeIfAbsent(manager, Listeners::new);
        listeners.add(conditions);
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer<Instance> conditions) {
        Listeners listeners = this.listeners.get(manager);
        if (listeners != null) {
            listeners.remove(conditions);
            if (listeners.isEmpty()) {
                this.listeners.remove(manager);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker tracker) {
        this.listeners.remove(tracker);
    }

    @Override
    public Instance conditionsFromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Instance(this.id);
    }

    public Instance createInstance() {
        return new Instance(this.id);
    }

    public void trigger(ServerPlayerEntity player) {
        Listeners listeners = this.listeners.get(player.getAdvancementTracker());
        if (listeners != null) {
            listeners.trigger();
        }
    }

    public static class Instance implements CriterionConditions {
        private final Identifier id;

        public Instance(Identifier id) {
            super();
            this.id = id;
        }

        @Override
        public Identifier getId() {
            return this.id;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            return new JsonObject();
        }
    }

    static class Listeners {
        private final Set<ConditionsContainer<Instance>> listeners = new HashSet<>();
        private final PlayerAdvancementTracker advancements;

        public Listeners(PlayerAdvancementTracker advancements) {
            this.advancements = advancements;
        }

        public void add(ConditionsContainer<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ConditionsContainer<Instance> listener) {
            this.listeners.remove(listener);
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void trigger() {
            List<ConditionsContainer<Instance>> listenerList = new ArrayList<>(this.listeners);
            for (ConditionsContainer<Instance> instanceListener : listenerList) {
                instanceListener.grant(this.advancements);
            }
        }
    }
}
