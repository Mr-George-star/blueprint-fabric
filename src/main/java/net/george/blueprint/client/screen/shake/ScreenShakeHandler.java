package net.george.blueprint.client.screen.shake;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.BlueprintConfig;
import net.george.blueprint.core.events.EntityViewRenderEvents;
import net.george.blueprint.core.events.WorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Handles the updating of {@link ShakeSource}s used to shake the screen.
 * <p>Individual {@link ShakeSource}s are updated, and their intensities get added together to get values used for shaking the screen.</p>
 * <p>This class is an enum to make it unable to be extended and to only have one instance ({@link #INSTANCE}).</p>
 *
 * @author Mr.George
 * @see ShakeSource
 */
public enum ScreenShakeHandler {
    INSTANCE;

    private static final Random RANDOM = new Random();
    private final List<ShakeSource> sources = new LinkedList<>();
    private double prevIntensityX, prevIntensityY, prevIntensityZ;
    private double intensityX, intensityY, intensityZ;

    private static double randomizeIntensity(double intensity) {
        double randomDouble = RANDOM.nextDouble();
        return (1.0D - randomDouble * randomDouble) * (RANDOM.nextInt(2) - 0.5D) * intensity * 2.0D;
    }

    /**
     * Tries to add a {@link ShakeSource} to the {@link #sources} list.
     * <p>Use this to add a {@link ShakeSource} to affect the screen shaking.</p>
     *
     * @param source A {@link ShakeSource} to add.
     * @return If the amount of sources doesn't exceed the maximum amount of shakers ({@link BlueprintConfig.Client#maxScreenShakers}).
     */
    @CanIgnoreReturnValue
    public boolean addShakeSource(ShakeSource source) {
        List<ShakeSource> sources = this.sources;
        if (sources.size() >= BlueprintConfig.CLIENT.maxScreenShakers) return false;
        sources.add(source);
        return true;
    }

    private void tick() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (!minecraft.isPaused()) {
            this.prevIntensityX = this.intensityX;
            this.prevIntensityY = this.intensityY;
            this.prevIntensityZ = this.intensityZ;
            List<ShakeSource> sources = this.sources;
            if (sources.isEmpty()) {
                this.intensityX = this.intensityY = this.intensityZ = 0.0F;
            } else {
                Iterator<ShakeSource> sourceIterator = sources.iterator();
                Entity entity = minecraft.cameraEntity;
                Vec3d pos = entity != null ? entity.getPos() : Vec3d.ZERO;
                double intensityX = 0.0F, intensityY = 0.0F, intensityZ = 0.0F;
                while (sourceIterator.hasNext()) {
                    ShakeSource shakingSource = sourceIterator.next();
                    shakingSource.tick();
                    if (shakingSource.isStopped()) {
                        sourceIterator.remove();
                    } else {
                        Vec3d intensity = shakingSource.getIntensity(pos);
                        double newIntensityX = intensityX + intensity.x;
                        double maxX = shakingSource.getMaxBuildupX();
                        if (newIntensityX <= maxX) {
                            intensityX = newIntensityX;
                        } else if (maxX > intensityX) {
                            intensityX = maxX;
                        }
                        double newIntensityY = intensityY + intensity.y;
                        double maxY = shakingSource.getMaxBuildupY();
                        if (newIntensityY <= maxY) {
                            intensityY = newIntensityY;
                        } else if (maxY > intensityY) {
                            intensityY = maxY;
                        }
                        double newIntensityZ = intensityZ + intensity.z;
                        double maxZ = shakingSource.getMaxBuildupZ();
                        if (newIntensityZ <= maxZ) {
                            intensityZ = newIntensityZ;
                        } else if (maxZ > intensityZ) {
                            intensityZ = maxZ;
                        }
                    }
                }
                this.intensityX = intensityX != 0.0F ? randomizeIntensity(intensityX) : 0.0F;
                this.intensityY = intensityY != 0.0F ? randomizeIntensity(intensityY) : 0.0F;
                this.intensityZ = intensityZ != 0.0F ? randomizeIntensity(intensityZ) : 0.0F;
            }
        }
    }

    private void shakeCamera(GameRenderer renderer, Camera camera, double tickDelta, float yaw, float pitch, float roll) {
        double screenShakeScale = BlueprintConfig.CLIENT.screenShakeScale;
        if (screenShakeScale > 0.0D) {
            double x = MathHelper.lerp(tickDelta, this.prevIntensityX, this.intensityX), y = MathHelper.lerp(tickDelta, this.prevIntensityY, this.intensityY), z = MathHelper.lerp(tickDelta, this.prevIntensityZ, this.intensityZ);
            if (x != 0.0F || y != 0.0F || z != 0.0F) {
                try {
                    camera.moveBy(z * screenShakeScale, y * screenShakeScale, x * screenShakeScale);
                } catch (Throwable exception) {
                    Blueprint.LOGGER.info("Screen Shake Handler: Can't shake screen!", exception);
                }
            }
        }
    }

    public static void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> INSTANCE.tick());
        ServerLoginConnectionEvents.DISCONNECT.register((handler, server) -> INSTANCE.clear());
        ServerWorldEvents.UNLOAD.register((server, world) -> INSTANCE.clear());
        WorldEvents.UNLOAD.register(world -> INSTANCE.clear());
        EntityViewRenderEvents.CAMERA_SETUP.register(INSTANCE::shakeCamera);
    }

    private void clear() {
        this.sources.clear();
        this.prevIntensityX = this.prevIntensityY = this.prevIntensityZ = this.intensityX = this.intensityY = this.intensityZ = 0.0F;
    }
}
