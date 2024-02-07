package net.george.blueprint.core.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.common.network.MessageC2SUpdateSlabfishHat;
import net.george.blueprint.common.network.entity.MessageS2CEndimation;
import net.george.blueprint.common.network.entity.MessageS2CTeleportEntity;
import net.george.blueprint.common.network.entity.MessageS2CUpdateEntityData;
import net.george.blueprint.common.network.praticle.MessageS2CSpawnParticle;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.endimator.Endimatable;
import net.george.blueprint.core.endimator.PlayableEndimation;
import net.george.blueprint.core.endimator.PlayableEndimationManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * A utility class containing some useful Minecraft networking methods.
 *
 * @author SmellyModder(Luke Tonon)
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public class NetworkUtil {
    /**
     * All other parameters work the same in {@link World#addParticle(ParticleEffect, double, double, double, double, double, double)}.
     * <p>Used for adding particles to client levels from the server side.</p>
     *
     * @param name    The registry name of the particle.
     * @param posX    The x pos of the particle.
     * @param posY    The y pos of the particle.
     * @param posZ    The z pos of the particle.
     * @param motionX The x motion of the particle.
     * @param motionY The y motion of the particle.
     * @param motionZ The y motion of the particle.
     */
    public static void spawnParticle(String name, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        Blueprint.CHANNEL.sendToClientsInCurrentServer(new MessageS2CSpawnParticle(name, posX, posY, posZ, motionX, motionY, motionZ));
    }

    /**
     * All other parameters work the same in {@link World#addParticle(ParticleEffect, double, double, double, double, double, double)}.
     * <p>Used for adding particles to client levels from the server side.</p>
     * <p>Only sends the packet to players in {@code serverWorld}.</p>
     *
     * @param name      The registry name of the particle.
     * @param serverWorld The world to spawn the particle in.
     * @param posX      The x pos of the particle.
     * @param posY      The y pos of the particle.
     * @param posZ      The z pos of the particle.
     * @param motionX   The x motion of the particle.
     * @param motionY   The y motion of the particle.
     * @param motionZ   The y motion of the particle.
     */
    public static void spawnParticle(String name, ServerWorld serverWorld, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        Blueprint.CHANNEL.sendToClients(new MessageS2CSpawnParticle(name, posX, posY, posZ, motionX, motionY, motionZ), PlayerLookup.world(serverWorld));
    }

    /**
     * Teleports the entity to a specified location.
     *
     * @param entity The Entity to teleport.
     * @param posX   The x position.
     * @param posY   The y position.
     * @param posZ   The z position.
     */
    public static void teleportEntity(Entity entity, double posX, double posY, double posZ) {
        entity.refreshPositionAndAngles(posX, posY, posZ, entity.getYaw(), entity.getPitch());
        Blueprint.CHANNEL.sendToClientsInCurrentServer(new MessageS2CTeleportEntity(entity.getId(), posX, posY, posZ));
    }

    /**
     * Sends an animation message to the clients to update an entity's animations.
     *
     * @param entity           The Entity to send the packet for.
     * @param endimationToPlay The endimation to play.
     */
    public static <E extends Entity & Endimatable> void setPlayingAnimation(E entity, PlayableEndimation endimationToPlay) {
        if (!entity.world.isClient) {
            Blueprint.CHANNEL.sendToClientsTrackingAndSelf(new MessageS2CEndimation(entity.getId(), PlayableEndimationManager.INSTANCE.getID(endimationToPlay)), entity);
            entity.setPlayingEndimation(endimationToPlay);
        }
    }

    /**
     * Sends a {@link MessageS2CUpdateEntityData} instance to the player to update a tracked entity's {@link IDataManager} values.
     *
     * @param player   A {@link ServerPlayerEntity} to send the message to.
     * @param targetID The ID of the entity to update.
     * @param entries  A set of new entries.
     */
    public static void updateTrackedData(ServerPlayerEntity player, int targetID, Set<IDataManager.DataEntry<?>> entries) {
        Blueprint.CHANNEL.sendToClient(new MessageS2CUpdateEntityData(targetID, entries), player);
    }

    /**
     * Sends a {@link MessageS2CUpdateEntityData} instance to an entity to update its {@link IDataManager} values.
     *
     * @param entity  An {@link Entity} to update.
     * @param entries A set of new entries.
     */
    public static void updateTrackedData(Entity entity, Set<IDataManager.DataEntry<?>> entries) {
        Blueprint.CHANNEL.sendToClientsTracking(new MessageS2CUpdateEntityData(entity.getId(), entries), entity);
    }

    /**
     * Sends a {@link MessageC2SUpdateSlabfishHat} to the server to update the sender's slabfish hat settings.
     *
     * @param setting The new slabfish hat setting(s).
     */
    @Environment(EnvType.CLIENT)
    public static void updateSlabfish(byte setting) {
        if (ClientInfo.getClientPlayer() != null) {
            Blueprint.CHANNEL.sendToServer(new MessageC2SUpdateSlabfishHat(setting));
        }
    }
}
