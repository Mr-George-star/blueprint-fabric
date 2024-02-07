package net.george.blueprint.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType((found, constants) ->
            new TranslatableText("commands.forge.arguments.enum.invalid", constants, found));
    private final Class<T> enumClass;

    private EnumArgument(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    public static <R extends Enum<R>> EnumArgument<R> enumArgument(Class<R> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    public T parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();

        try {
            return Enum.valueOf(this.enumClass, name);
        } catch (IllegalArgumentException var4) {
            throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(this.enumClass.getEnumConstants()));
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Stream.of(this.enumClass.getEnumConstants()).map(Object::toString), builder);
    }

    public Collection<String> getExamples() {
        return Stream.of(this.enumClass.getEnumConstants()).map(Object::toString).collect(Collectors.toList());
    }

    public static class Serializer implements ArgumentSerializer<EnumArgument<?>> {
        public void toPacket(EnumArgument<?> argument, PacketByteBuf buffer) {
            buffer.writeString(argument.enumClass.getName());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public EnumArgument<?> fromPacket(PacketByteBuf buffer) {
            try {
                String name = buffer.readString();
                return new EnumArgument(Class.forName(name));
            } catch (ClassNotFoundException var3) {
                return null;
            }
        }

        public void toJson(EnumArgument<?> argument, JsonObject json) {
            json.addProperty("enum", argument.enumClass.getName());
        }
    }
}
