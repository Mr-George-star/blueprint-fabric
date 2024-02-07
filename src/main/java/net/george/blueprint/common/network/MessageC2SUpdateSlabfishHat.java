package net.george.blueprint.common.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.C2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Message for syncing Slabfish hat customization settings to the server.
 *
 * @author Jackson
 */
public final class MessageC2SUpdateSlabfishHat implements C2SPacket {
    private final byte setting;

    public MessageC2SUpdateSlabfishHat(byte setting) {
        this.setting = setting;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeByte(this.setting);
    }

    public static MessageC2SUpdateSlabfishHat decode(PacketByteBuf buf) {
        return new MessageC2SUpdateSlabfishHat(buf.readByte());
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        server.execute(() -> {
            if (player != null) {
                ((IDataManager)player).setValue(Blueprint.SLABFISH_SETTINGS, this.setting);
            }
        });
    }
}
