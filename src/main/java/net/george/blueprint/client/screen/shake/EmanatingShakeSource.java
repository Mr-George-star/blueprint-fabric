package net.george.blueprint.client.screen.shake;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Position;

import java.util.function.Supplier;

/**
 * An extension of {@link PositionedShakeSource} where the source is emanating from another object.
 * <p>The position of the source is updated to the supplier's position in {@link #tick()}</p>
 * <p>If {@link #isActive} returns false, the source will decay, eventually stopping after 20 ticks.</p>
 *
 * @author SmellyModder (Luke Tonon)
 * @see PositionedShakeSource
 * @see ScreenShakeHandler
 */
@SuppressWarnings("unused")
public class EmanatingShakeSource extends PositionedShakeSource {
    private final Supplier<Position> position;
    private final Supplier<Boolean> isActive;
    private int deadTicks;

    public EmanatingShakeSource(Supplier<Position> position, Supplier<Boolean> isActive, int duration, double intensityX, double intensityY, double intensityZ, double maxBuildupX, double maxBuildupY, double maxBuildupZ, double decayX, double decayY, double decayZ) {
        super(duration, intensityX, intensityY, intensityZ, maxBuildupX, maxBuildupY, maxBuildupZ, decayX, decayY, decayZ, position.get().getX(), position.get().getY(), position.get().getZ());
        this.position = position;
        this.isActive = isActive;
    }

    public EmanatingShakeSource(Supplier<Position> position, Supplier<Boolean> isAlive, int duration, double intensity, double maxBuildup, double decay) {
        super(duration, intensity, maxBuildup, decay, position.get().getX(), position.get().getY(), position.get().getZ());
        this.position = position;
        this.isActive = isAlive;
    }

    public EmanatingShakeSource(Entity entity, int duration, double intensityX, double intensityY, double intensityZ, double maxBuildupX, double maxBuildupY, double maxBuildupZ, double decayX, double decayY, double decayZ) {
        this(entity::getPos, entity::isAlive, duration, intensityX, intensityY, intensityZ, maxBuildupX, maxBuildupY, maxBuildupZ, decayX, decayY, decayZ);
    }

    @Override
    public void tick() {
        Position position = this.position.get();
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        if (!this.isActive.get()) {
            this.deadTicks++;
            this.decayX *= 0.925F;
            this.decayY *= 0.925F;
            this.decayZ *= 0.925F;
        }
        super.tick();
    }

    @Override
    public boolean isStopped() {
        return super.isStopped() || this.deadTicks >= 20;
    }
}
