package net.george.blueprint.core.endimator.effects.sound;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.george.blueprint.core.endimator.effects.EndimationEffect;
import net.george.blueprint.core.endimator.effects.EndimationEffectSource;

//TODO: Implement this
public final class SoundEndimationEffect extends EndimationEffect<Unit> {
    public SoundEndimationEffect() {
        super(Codec.unit(Unit.INSTANCE));
    }

    @Override
    public void process(EndimationEffectSource source, float time, Unit config) {
    }
}
