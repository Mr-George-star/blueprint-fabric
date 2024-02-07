package net.george.blueprint.core.util.modification.selection.selectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.george.blueprint.core.util.modification.selection.ResourceSelector;
import net.george.blueprint.core.util.modification.selection.ResourceSelectorSerializers;
import net.george.blueprint.core.util.modification.selection.SelectionSpace;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link ResourceSelector} implementation that returns a list of target names picked using a configurable regular expression.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record RegexResourceSelector(Pattern pattern) implements ResourceSelector<RegexResourceSelector> {
    public RegexResourceSelector(Pattern pattern) {
        this.pattern = pattern;
    }

    public List<Identifier> select(SelectionSpace space) {
        List<Identifier> targetNames = new ArrayList<>();
        Matcher matcher = this.pattern.matcher("");
        space.forEach((key) -> {
            if (matcher.reset(key.toString()).matches()) {
                targetNames.add(key);
            }

        });
        return targetNames;
    }

    public Serializer getSerializer() {
        return ResourceSelectorSerializers.REGEX;
    }

    public Pattern pattern() {
        return this.pattern;
    }

    /**
     * The serializer class for the {@link RegexResourceSelector}.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public static final class Serializer implements ResourceSelector.Serializer<RegexResourceSelector> {
        public Serializer() {
        }

        public JsonElement serialize(RegexResourceSelector selector) {
            return new JsonPrimitive(selector.pattern().pattern());
        }

        public RegexResourceSelector deserialize(JsonElement element) {
            return new RegexResourceSelector(Pattern.compile(element.getAsString()));
        }
    }
}
