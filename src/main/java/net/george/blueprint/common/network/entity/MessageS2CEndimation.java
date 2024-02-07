package net.george.blueprint.common.network.entity;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.george.blueprint.core.endimator.Endimatable;
import net.george.blueprint.core.endimator.PlayableEndimation;
import net.george.blueprint.core.endimator.PlayableEndimationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

/**
 * The message for telling clients to begin playing a {@link PlayableEndimation} on an {@link Endimatable} entity.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class MessageS2CEndimation implements S2CPacket {
    private final int entityId;
    private final int endimationId;

    public MessageS2CEndimation(int entityID, int endimationId) {
        this.entityId = entityID;
        this.endimationId = endimationId;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.endimationId);
    }

    public static MessageS2CEndimation decode(PacketByteBuf buf) {
        return new MessageS2CEndimation(buf.readInt(), buf.readInt());
    }

    @Override
    public void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        Endimatable endimatedEntity = (Endimatable) ClientInfo.getClientPlayerWorld().getEntityById(this.entityId);
        if (endimatedEntity != null) {
            int id = this.endimationId;
            PlayableEndimation endimation = PlayableEndimationManager.INSTANCE.getEndimation(id);
            if (endimation == null) {
                Blueprint.LOGGER.warn("Could not find Playable Endimation with ID " + id + " to play, defaulting to blank.");
                endimatedEntity.resetEndimation();
            } else {
                endimatedEntity.setPlayingEndimation(endimation);
            }
        }
    }
}
