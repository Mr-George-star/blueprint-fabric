package net.george.blueprint.core.util.modification.selection.selectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.george.blueprint.core.util.modification.selection.ResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelectorSerializers;
import net.george.blueprint.core.util.modification.selection.SelectionSpace;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link ResourceSelector} implementation that returns a configurable list of target names.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record NamesResourceSelector(List<Identifier> names) implements ResourceSelector<NamesResourceSelector> {
    public NamesResourceSelector(Identifier... names) {
        this(List.of(names));
    }

    public NamesResourceSelector(String... names) {
        this(Stream.of(names).map(Identifier::new).toList());
    }

    public NamesResourceSelector(List<Identifier> names) {
        this.names = names;
    }

    public List<Identifier> select(SelectionSpace space) {
        return this.names;
    }

    public Serializer getSerializer() {
        return ResourceSelectorSerializers.NAMES;
    }

    public List<Identifier> names() {
        return this.names;
    }

    /**
     * The serializer class for the {@link NamesResourceSelector}.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public static final class Serializer implements ResourceSelector.Serializer<NamesResourceSelector> {
        public Serializer() {
        }

        public JsonElement serialize(NamesResourceSelector selector) {
            JsonArray jsonArray = new JsonArray();
            selector.names.forEach((location) -> jsonArray.add(location.toString()));
            return jsonArray;
        }

        public NamesResourceSelector deserialize(JsonElement element) {
            JsonArray jsonArray = element.getAsJsonArray();
            List<Identifier> names = new ArrayList<>();
            jsonArray.forEach((nameElement) -> names.add(new Identifier(nameElement.getAsString())));
            return new NamesResourceSelector(names);
        }
    }
}
