package net.george.blueprint.client.model.generator;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for item models, adds the ability to build overrides via
 * {@link #override()}.
 */
@SuppressWarnings("unused")
public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {
    protected List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    public OverrideBuilder override() {
        OverrideBuilder builder = new OverrideBuilder();
        this.overrides.add(builder);
        return builder;
    }

    /**
     * Get an existing override builder
     *
     * @param index the index of the existing override builder
     * @return the override builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public OverrideBuilder override(int index) {
        Preconditions.checkElementIndex(index, this.overrides.size(), "override");
        return this.overrides.get(index);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if (!this.overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            this.overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
            root.add("overrides", overridesJson);
        }
        return root;
    }

    public class OverrideBuilder {
        private ModelFile model;
        private final Map<Identifier, Float> predicates = new LinkedHashMap<>();

        public OverrideBuilder model(ModelFile model) {
            this.model = model;
            model.assertExistence();
            return this;
        }

        public OverrideBuilder predicate(Identifier key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        public ItemModelBuilder end() {
            return ItemModelBuilder.this;
        }

        JsonObject toJson() {
            JsonObject result = new JsonObject();
            JsonObject predicatesJson = new JsonObject();
            this.predicates.forEach((key, value) -> predicatesJson.addProperty(key.toString(), value));
            result.add("predicate", predicatesJson);
            result.addProperty("model", this.model.getId().toString());
            return result;
        }
    }

}
