package net.george.blueprint.core.api.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

/**
 * A packet from server send to client.
 *
 * @author Mr.George
 */
public interface S2CPacket extends Packet {
    /**
     * This method will be run on the network thread. Most method calls should be performed on the client thread by wrapping the code in a lambda:
     * <pre>
     * <code>client.execute(() -> {
     * 	// code here
     * }</code></pre>
     */
    void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel);
}
