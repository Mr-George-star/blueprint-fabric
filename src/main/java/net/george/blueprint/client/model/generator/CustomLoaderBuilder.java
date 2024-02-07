package net.george.blueprint.client.model.generator;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class CustomLoaderBuilder<T extends ModelBuilder<T>> {
    protected final Identifier loaderId;
    protected final T parent;
    protected final ExistingFileHelper existingFileHelper;
    protected final Map<String, Boolean> visibility = new LinkedHashMap<>();

    protected CustomLoaderBuilder(Identifier loaderId, T parent, ExistingFileHelper existingFileHelper) {
        this.loaderId = loaderId;
        this.parent = parent;
        this.existingFileHelper = existingFileHelper;
    }

    public CustomLoaderBuilder<T> visibility(String partName, boolean show) {
        Preconditions.checkNotNull(partName, "partName must not be null");
        this.visibility.put(partName, show);
        return this;
    }

    public T end() {
        return this.parent;
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("loader", loaderId.toString());

        if (this.visibility.size() > 0) {
            JsonObject visibilityObject = new JsonObject();

            for(Map.Entry<String, Boolean> entry : this.visibility.entrySet()) {
                visibilityObject.addProperty(entry.getKey(), entry.getValue());
            }

            json.add("visibility", visibilityObject);
        }

        return json;
    }
}
