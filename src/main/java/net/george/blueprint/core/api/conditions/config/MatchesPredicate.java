package net.george.blueprint.core.api.conditions.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.config.ForgeConfigSpec;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * A config predicate that checks whether the string got from a {@link ForgeConfigSpec.ConfigValue} instance matches a stored regular expression.
 * <p>Throws an exception if the config value type is not {@code String}.</p>
 *
 * @author abigailfails
 */
public class MatchesPredicate implements IConfigPredicate {
    private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "matches");
    private final String regex;

    public MatchesPredicate(String regex) {
        this.regex = regex;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean test(ForgeConfigSpec.ConfigValue<?> toCompare) {
        if (toCompare.get() instanceof String) {
            return ((String)toCompare.get()).matches(regex);
        }
        throw new IllegalArgumentException("Invalid config value type; must hold a String");
    }

    public static class Serializer implements IConfigPredicateSerializer<MatchesPredicate> {
        private static final Identifier ID = new Identifier(Blueprint.MOD_ID, "matches");

        @Override
        public void write(JsonObject json, IConfigPredicate value) {
            if (!(value instanceof MatchesPredicate)) throw new IllegalArgumentException("Incompatible predicate type");
            json.addProperty("expression", ((MatchesPredicate) value).regex);
        }

        @Override
        public MatchesPredicate read(JsonObject json) {
            if (!json.has("expression") && !JsonHelper.hasString(json, "expression"))
                throw new JsonSyntaxException("Missing 'expression', expected to find a regular expression");
            return new MatchesPredicate(json.get("expression").getAsString());
        }

        @Override
        public Identifier getId() {
            return ID;
        }
    }
}
