package net.george.blueprint.core.endimator;

import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

/**
 * A record class that represents a loop-able animation processed over a given tick duration.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public record PlayableEndimation(Identifier identifier, int duration, LoopType loopType) {
    public static final PlayableEndimation BLANK;

    public PlayableEndimation(Identifier identifier, int duration, LoopType loopType) {
        this.identifier = identifier;
        this.duration = duration;
        this.loopType = loopType;
    }

    /**
     * Looks up the {@link #identifier} in {@link Blueprint#ENDIMATION_LOADER} to get its corresponding {@link Endimation}.
     *
     * @return The corresponding {@link Endimation} of the {@link #identifier}.
     */
    @Nullable
    public Endimation asEndimation() {
        return Blueprint.ENDIMATION_LOADER.getEndimation(this.identifier);
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public int duration() {
        return this.duration;
    }

    public LoopType loopType() {
        return this.loopType;
    }

    static {
        BLANK = new PlayableEndimation(new Identifier(Blueprint.MOD_ID, "blank"), 0, LoopType.NONE);
    }

    /**
     * This enum represents the three types of animation looping that Endimator supports.
     * <p>{@link #NONE} for no looping.</p>
     * <p>{@link #LOOP} to loop.</p>
     * <p>{@link #HOLD} to hold on the last tick.</p>
     *
     * @author SmellyModder (Luke Tonon)
     */
    public enum LoopType {
        NONE,
        LOOP,
        HOLD;
    }
}

