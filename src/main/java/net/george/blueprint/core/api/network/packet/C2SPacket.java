package net.george.blueprint.core.api.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A packet from client send to server.
 *
 * @author Mr.George
 */
public interface C2SPacket extends Packet {
    /**
     * This method will be run on the network thread. Most method calls should be performed on the server thread by wrapping the code in a lambda:
     * <pre>
     * <code>server.execute(() -> {
     * 	// code here
     * }</code></pre>
     */
    void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel);
}
