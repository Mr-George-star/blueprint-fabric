package net.george.blueprint.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class containing some useful methods for getting information about the client.
 *
 * @author Mr.George
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class ClientInfo {
    public static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();

    /**
     * Gets the partial ticks of the client.
     *
     * @return The partial ticks of the client.
     */
    public static float getPartialTicks() {
        return MINECRAFT.isPaused() ? MINECRAFT.pausedTickDelta : MINECRAFT.getTickDelta();
    }

    /**
     * Gets the {@link ClientPlayerEntity} entity.
     *
     * @return The {@link ClientPlayerEntity} entity.
     */
    public static ClientPlayerEntity getClientPlayer() {
        return MINECRAFT.player;
    }

    /**
     * Gets the {@link World} of the client player.
     *
     * @return The client player's world; equivalent to getting the client world.
     */
    public static World getClientPlayerWorld() {
        return ClientInfo.getClientPlayer().world;
    }
}
