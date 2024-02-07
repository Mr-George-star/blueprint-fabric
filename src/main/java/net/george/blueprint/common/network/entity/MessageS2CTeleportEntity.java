package net.george.blueprint.common.network.entity;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

/**
 * The message for teleporting the entity from the server.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class MessageS2CTeleportEntity implements S2CPacket {
    private final int entityId;
    private final double posX, posY, posZ;

    public MessageS2CTeleportEntity(int entityID, double posX, double posY, double posZ) {
        this.entityId = entityID;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
    }

    public static MessageS2CTeleportEntity decode(PacketByteBuf buf) {
        int entityId = buf.readInt();
        return new MessageS2CTeleportEntity(entityId, buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            Entity entity = ClientInfo.getClientPlayerWorld().getEntityById(this.entityId);
            if (entity != null) {
                entity.refreshPositionAndAngles(this.posX, this.posY, this.posZ, entity.getYaw(), entity.getPitch());
            }
        });
    }
}
