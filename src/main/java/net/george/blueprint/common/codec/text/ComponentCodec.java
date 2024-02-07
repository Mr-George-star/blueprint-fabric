package net.george.blueprint.common.codec.text;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A codec for {@link Text}s.
 * <p>Current missing the ability to serialize styles.</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public enum ComponentCodec implements Codec<Text> {
    INSTANCE;

    private static <T> Either<String, String> getString(DynamicOps<T> ops, T value) {
        DataResult<String> stringDataResult = ops.getStringValue(value);
        Optional<DataResult.PartialResult<String>> error = stringDataResult.error();
        return error.<Either<String, String>>map(stringPartialResult -> Either.right(stringPartialResult.message())).orElseGet(() -> Either.left(stringDataResult.result().get()));
    }

    private static <T> boolean has(@Nonnull MapLike<T> mapLike, @Nonnull String key) {
        return mapLike.get(key) != null;
    }

    @Override
    public <T> DataResult<Pair<Text, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<String> stringDataResult = ops.getStringValue(input);
        if (stringDataResult.error().isEmpty()) {
            return DataResult.success(Pair.of(new LiteralText(stringDataResult.result().get()), input));
        }
        DataResult<MapLike<T>> mapLikeDataResult = ops.getMap(input);
        if (mapLikeDataResult.error().isEmpty()) {
            Optional<MapLike<T>> optional = mapLikeDataResult.result();
            if (optional.isPresent()) {
                MutableText formattableTextComponent;
                MapLike<T> mapLike = optional.get();
                if (has(mapLike, "text")) {
                    Either<String, String> textOrError = getString(ops, mapLike.get("text"));
                    if (textOrError.left().isPresent()) {
                        formattableTextComponent = new LiteralText(textOrError.left().get());
                    } else {
                        return DataResult.error(textOrError.right().get());
                    }
                } else {
                    String string;
                    if (has(mapLike, "translate")) {
                        Optional<String> stringOptional = ops.getStringValue(mapLike.get("translate")).result();
                        if (stringOptional.isPresent()) {
                            string = stringOptional.get();
                            if (has(mapLike, "with")) {
                                Optional<Stream<T>> withStream = ops.getStream(mapLike.get("with")).result();
                                if (withStream.isPresent()) {
                                    Stream<T> stream = withStream.get();
                                    List<T> list = stream.toList();
                                    Object[] objects = new Object[list.size()];
                                    for (int i = 0; i < objects.length; i++) {
                                        DataResult<Pair<Text, T>> dataResult = this.decode(ops, list.get(i));
                                        Optional<DataResult.PartialResult<Pair<Text, T>>> error = dataResult.error();
                                        if (error.isPresent()) {
                                            return DataResult.error(error.get().message());
                                        } else {
                                            objects[i] = dataResult.result().get();
                                            if (objects[i] instanceof LiteralText stringTextComponent) {
                                                if (stringTextComponent.getStyle().isEmpty() && stringTextComponent.getSiblings().isEmpty()) {
                                                    objects[i] = stringTextComponent.getRawString();
                                                }
                                            }
                                        }
                                    }
                                    formattableTextComponent = new TranslatableText(string, objects);
                                } else {
                                    return DataResult.error("Expected 'with' to be a JsonArray");
                                }
                            } else {
                                formattableTextComponent = new TranslatableText(string);
                            }
                        } else {
                            return DataResult.error("Missing 'translate', expected to find string");
                        }
                    } else if (has(mapLike, "score")) {
                        Optional<MapLike<T>> optionalScoreMap = ops.getMap(mapLike.get("score")).result();
                        if (optionalScoreMap.isPresent()) {
                            MapLike<T> scoreMap = optionalScoreMap.get();
                            if (!has(scoreMap, "name") || !has(scoreMap, "objective")) {
                                return DataResult.error("A score component needs at least a name and an objective");
                            }
                            Either<String, String> stringNameOrError = getString(ops, scoreMap.get("name"));
                            if (stringNameOrError.right().isPresent()) {
                                return DataResult.error(stringNameOrError.right().get());
                            } else {
                                Either<String, String> errorOrStringObjective = getString(ops, scoreMap.get("objective"));
                                if (errorOrStringObjective.right().isPresent()) {
                                    return DataResult.error(errorOrStringObjective.right().get());
                                }
                                formattableTextComponent = new ScoreText(stringNameOrError.left().get(), errorOrStringObjective.left().get());
                            }
                        } else {
                            return DataResult.error("Expected 'score' to be a JsonObject");
                        }
                    } else if (has(mapLike, "selector")) {
                        Either<String, String> selectorOrError = getString(ops, mapLike.get("selector"));
                        if (selectorOrError.left().isPresent()) {
                            formattableTextComponent = new SelectorText(selectorOrError.left().get(), Optional.empty());
                        } else {
                            return DataResult.error(selectorOrError.right().get());
                        }
                    } else if (has(mapLike, "keybind")) {
                        Either<String, String> selectorOrError = getString(ops, mapLike.get("keybind"));
                        if (selectorOrError.left().isPresent()) {
                            formattableTextComponent = new KeybindText(selectorOrError.left().get());
                        } else {
                            return DataResult.error(selectorOrError.right().get());
                        }
                    } else {
                        if (!has(mapLike, "nbt")) {
                            return DataResult.error("Don't know how to turn " + mapLike + " into a Component");
                        }

                        Either<String, String> nbtOrError = getString(ops, mapLike.get("nbt"));
                        if (nbtOrError.left().isPresent()) {
                            string = nbtOrError.left().get();
                            boolean interpret = has(mapLike, "interpret");
                            if (interpret) {
                                DataResult<Boolean> interpretResult = ops.getBooleanValue(mapLike.get("interpret"));
                                if (interpretResult.error().isPresent()) {
                                    return DataResult.error("Expected 'interpret' to be a boolean");
                                } else {
                                    interpret = interpretResult.result().get();
                                }
                            }
                            if (has(mapLike, "block")) {
                                Either<String, String> blockOrError = getString(ops, mapLike.get("block"));
                                if (blockOrError.left().isPresent()) {
                                    formattableTextComponent = new NbtText.BlockNbtText(string, interpret, blockOrError.left().get(), Optional.empty());
                                } else {
                                    return DataResult.error(blockOrError.right().get());
                                }
                            } else if (has(mapLike, "entity")) {
                                Either<String, String> entityOrError = getString(ops, mapLike.get("entity"));
                                if (entityOrError.left().isPresent()) {
                                    formattableTextComponent = new NbtText.EntityNbtText(string, interpret, entityOrError.left().get(), Optional.empty());
                                } else {
                                    return DataResult.error(entityOrError.right().get());
                                }
                            } else {
                                if (!has(mapLike, "storage")) {
                                    return DataResult.error("Don't know how to turn " + mapLike + " into a Component");
                                }

                                Either<String, String> storageOrError = getString(ops, mapLike.get("storage"));
                                if (storageOrError.left().isPresent()) {
                                    formattableTextComponent = new NbtText.StorageNbtText(string, interpret, new Identifier(storageOrError.left().get()), Optional.empty());
                                } else {
                                    return DataResult.error(storageOrError.right().get());
                                }
                            }
                        } else {
                            return DataResult.error(nbtOrError.right().get());
                        }
                    }
                }

                T extra = mapLike.get("extra");
                if (extra != null) {
                    DataResult<Stream<T>> extraResult = ops.getStream(extra);
                    Optional<DataResult.PartialResult<Stream<T>>> error = extraResult.error();
                    if (error.isPresent()) {
                        return DataResult.error(error.get().message());
                    } else {
                        Stream<T> extraStream = extraResult.result().get();
                        List<T> entries = extraStream.toList();
                        if (entries.isEmpty()) {
                            return DataResult.error("Unexpected empty array of components");
                        } else {
                            for (T entry : entries) {
                                DataResult<Pair<Text, T>> entryResult = this.decode(ops, entry);
                                Optional<DataResult.PartialResult<Pair<Text, T>>> entryError = entryResult.error();
                                if (entryError.isPresent()) {
                                    return DataResult.error(entryError.get().message());
                                }
                                formattableTextComponent.append(entryResult.result().get().getFirst());
                            }
                        }
                    }
                }

                //TODO: Add style support
                return DataResult.success(Pair.of(formattableTextComponent, input));
            }
        }
        DataResult<Stream<T>> streamDataResult = ops.getStream(input);
        Optional<DataResult.PartialResult<Stream<T>>> error = streamDataResult.error();
        if (error.isPresent()) {
            return DataResult.error("Don't know how to turn " + input + " into a Component");
        }
        Optional<Stream<T>> stringOptional = streamDataResult.result();
        if (stringOptional.isPresent()) {
            MutableText component = new MarkerTextComponent();
            for (T entry : stringOptional.get().toList()) {
                DataResult<Pair<Text, T>> entryResult = this.decode(ops, entry);
                Optional<DataResult.PartialResult<Pair<Text, T>>> entryError = entryResult.error();
                if (entryError.isPresent()) {
                    return DataResult.error(entryError.get().message());
                } else {
                    Optional<Pair<Text, T>> optional = entryResult.result();
                    if (optional.isPresent()) {
                        if (component instanceof MarkerTextComponent) {
                            component = (MutableText) optional.get().getFirst();
                        } else {
                            component.append(optional.get().getFirst());
                        }
                    } else {
                        return DataResult.error("No Component found in " + entry);
                    }
                }
            }
            return DataResult.success(Pair.of(component, input));
        }
        return DataResult.error("Don't know how to turn " + input + " into a Component");
    }

    @Override
    public <T> DataResult<T> encode(Text input, DynamicOps<T> ops, T prefix) {
        RecordBuilder<T> mapBuilder = ops.mapBuilder();

        //TODO: Add style support
        if (!input.getSiblings().isEmpty()) {
            ListBuilder<T> siblings = ops.listBuilder();
            for (Text sibling : input.getSiblings()) {
                DataResult<T> encodedSibling = this.encode(sibling, ops, ops.empty());
                if (encodedSibling.error().isPresent()) {
                    return DataResult.error(encodedSibling.error().get().message());
                }
                siblings.add(encodedSibling);
            }
            mapBuilder.add("extra", siblings.build(ops.empty()));
        }

        if (input instanceof LiteralText) {
            mapBuilder.add("text", ops.createString(((LiteralText) input).getRawString()));
        } else if (input instanceof TranslatableText translationTextComponent) {
            mapBuilder.add("translate", ops.createString(((TranslatableText) input).getKey()));
            Object[] formatArgs = translationTextComponent.getArgs();
            if (formatArgs != null && formatArgs.length > 0) {
                ListBuilder<T> with = ops.listBuilder();
                for (Object arg : formatArgs) {
                    if (arg instanceof Text) {
                        DataResult<T> encodedArg = this.encode((Text) arg, ops, ops.empty());
                        if (encodedArg.error().isPresent()) {
                            return DataResult.error(encodedArg.error().get().message());
                        }
                        with.add(encodedArg);
                    } else {
                        with.add(ops.createString(String.valueOf(arg)));
                    }
                }
                mapBuilder.add("with", with.build(ops.empty()));
            }
        } else if (input instanceof ScoreText scoreTextComponent) {
            RecordBuilder<T> scoreMapBuilder = ops.mapBuilder();
            scoreMapBuilder.add("name", ops.createString(scoreTextComponent.getName()));
            scoreMapBuilder.add("objective", ops.createString(scoreTextComponent.getObjective()));
            mapBuilder.add("score", scoreMapBuilder.build(ops.empty()));
        } else if (input instanceof SelectorText) {
            mapBuilder.add("selector", ops.createString(((SelectorText) input).getPattern()));
        } else if (input instanceof KeybindText) {
            mapBuilder.add("keybind", ops.createString(((KeybindText) input).getKey()));
        } else {
            if (!(input instanceof NbtText nbtTextComponent)) {
                return DataResult.error("Don't know how to encode " + input + " as a Component");
            }

            mapBuilder.add("nbt", ops.createString(nbtTextComponent.getPath()));
            mapBuilder.add("interpret", ops.createBoolean(nbtTextComponent.shouldInterpret()));
            if (nbtTextComponent instanceof NbtText.BlockNbtText) {
                mapBuilder.add("block", ops.createString(((NbtText.BlockNbtText) nbtTextComponent).getPos()));
            } else if (nbtTextComponent instanceof NbtText.EntityNbtText) {
                mapBuilder.add("entity", ops.createString(((NbtText.EntityNbtText) nbtTextComponent).getSelector()));
            } else {
                if (!(nbtTextComponent instanceof NbtText.StorageNbtText)) {
                    return DataResult.error("Don't know to encode " + nbtTextComponent + " as a Component");
                }
                mapBuilder.add("storage", ops.createString(((NbtText.StorageNbtText) nbtTextComponent).getId().toString()));
            }
        }
        return mapBuilder.build(prefix);
    }

    static class MarkerTextComponent extends LiteralText {
        public MarkerTextComponent() {
            super("");
        }
    }
}
