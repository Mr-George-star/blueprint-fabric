package net.george.blueprint.common.server;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class LogicalSidedProvider<T> {
    public static final LogicalSidedProvider<ThreadExecutor<? super ServerTask>> WORKQUEUE = new LogicalSidedProvider<>(Supplier::get, Supplier::get);
    public static final LogicalSidedProvider<Optional<World>> CLIENT_WORLD = new LogicalSidedProvider<>(client -> Optional.ofNullable(client.get().world), server -> Optional.empty());
    private final Function<Supplier<MinecraftClient>, T> clientSide;
    private final Function<Supplier<MinecraftServer>, T> serverSide;
    private static Supplier<MinecraftClient> client;
    private static Supplier<MinecraftServer> server;

    private LogicalSidedProvider(Function<Supplier<MinecraftClient>, T> clientSide, Function<Supplier<MinecraftServer>, T> serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }

    public static void setClient(Supplier<MinecraftClient> client) {
        LogicalSidedProvider.client = client;
    }

    public static void setServer(Supplier<MinecraftServer> server) {
        LogicalSidedProvider.server = server;
    }

    public T get(final EnvType side) {
        return side == EnvType.CLIENT ? clientSide.apply(client) : serverSide.apply(server);
    }
}

