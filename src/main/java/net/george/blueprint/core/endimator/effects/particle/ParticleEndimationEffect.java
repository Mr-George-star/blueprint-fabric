package net.george.blueprint.core.endimator.effects.particle;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.george.blueprint.core.endimator.effects.EndimationEffect;
import net.george.blueprint.core.endimator.effects.EndimationEffectSource;

//TODO: Implement this
public final class ParticleEndimationEffect extends EndimationEffect<Unit> {
    public ParticleEndimationEffect() {
        super(Codec.unit(Unit.INSTANCE));
    }

    @Override
    public void process(EndimationEffectSource source, float time, Unit config) {
    }
}
