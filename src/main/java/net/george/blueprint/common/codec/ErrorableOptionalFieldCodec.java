package net.george.blueprint.common.codec;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.OptionalFieldCodec;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link MapCodec} extension similar to {@link OptionalFieldCodec} with the difference being errors will not get ignored when decoding.
 * <p>{@link OptionalFieldCodec} is not very user friendly as it doesn't explain why values get defaulted if an error occurs when decoding.</p>
 *
 * @param <A> The type of object for the codec.
 * @author SmellyModder (Luke Tonon)
 * @see OptionalFieldCodec
 */
public class ErrorableOptionalFieldCodec<A> extends MapCodec<Optional<A>> {
    private final String name;
    private final Codec<A> elementCodec;

    public ErrorableOptionalFieldCodec(String name, Codec<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    public static <A> MapCodec<A> errorableOptional(String name, Codec<A> codec, A defaultValue) {
        return new ErrorableOptionalFieldCodec<>(name, codec).xmap(
                optional -> optional.orElse(defaultValue),
                value -> Objects.equals(value, defaultValue) ? Optional.empty() : Optional.of(value)
        );
    }

    @Override
    public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
        T value = input.get(this.name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        DataResult<A> parsed = this.elementCodec.parse(ops, value);
        if (parsed.result().isPresent()) {
            return parsed.map(Optional::of);
        }
        return DataResult.error(parsed.error().get().message());
    }

    @Override
    public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            return prefix.add(this.name, this.elementCodec.encodeStart(ops, input.get()));
        }
        return prefix;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.name));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ErrorableOptionalFieldCodec<?> that = (ErrorableOptionalFieldCodec<?>) obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.elementCodec);
    }

    @Override
    public String toString() {
        return "ErrorableOptionalFieldCodec[" + this.name + ": " + this.elementCodec + ']';
    }
}
