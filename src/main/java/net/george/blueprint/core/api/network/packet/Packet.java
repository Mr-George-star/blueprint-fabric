package net.george.blueprint.core.api.network.packet;

import net.minecraft.network.PacketByteBuf;

/**
 * Parent class of {@link C2SPacket} and {@link S2CPacket}.
 *
 * @author Mr.George
 */
public interface Packet {
    /**
     * Write data to {@link PacketByteBuf}.
     * @param buf Data will be written to this {@link PacketByteBuf}.
     */
    void encode(PacketByteBuf buf);
}
