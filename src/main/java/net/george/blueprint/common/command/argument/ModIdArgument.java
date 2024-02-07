package net.george.blueprint.common.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.Blueprint;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ModIdArgument implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Arrays.asList(Blueprint.MOD_ID, "create");

    public ModIdArgument() {
    }

    public static ModIdArgument modIdArgument() {
        return new ModIdArgument();
    }

    public String parse(StringReader reader) {
        return reader.readUnquotedString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FabricLoader.getInstance().getAllMods().stream().map((container) ->
                container.getMetadata().getId()).collect(Collectors.toList()), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
