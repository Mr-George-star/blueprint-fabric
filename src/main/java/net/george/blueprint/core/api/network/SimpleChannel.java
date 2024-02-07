package net.george.blueprint.core.api.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.george.blueprint.common.server.ServerLifecycleHooks;
import net.george.blueprint.core.api.network.packet.C2SPacket;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SimpleChannel {
    private static final Logger LOGGER = LogManager.getLogger("Simple Channel");

    private final Identifier channelName;
    private final Map<Class<? extends C2SPacket>, Integer> c2sIdMap = new HashMap<>();
    private final Map<Class<? extends S2CPacket>, Integer> s2cIdMap = new HashMap<>();
    private final Int2ObjectMap<Function<PacketByteBuf, ? extends C2SPacket>> c2sDecoderMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Function<PacketByteBuf, ? extends S2CPacket>> s2cDecoderMap = new Int2ObjectOpenHashMap<>();

    public SimpleChannel(Identifier channelName) {
        this.channelName = channelName;
    }

    public void initServerListener() {
        C2SHandler c2sHandler = new C2SHandler();
        ServerPlayNetworking.registerGlobalReceiver(channelName, c2sHandler);
    }

    @Environment(EnvType.CLIENT)
    public void initClientListener() {
        S2CHandler s2cHandler = new S2CHandler();
        ClientPlayNetworking.registerGlobalReceiver(channelName, s2cHandler);
    }

    public <T extends C2SPacket> void registerC2SPacket(Class<T> clazz, int id, Function<PacketByteBuf, T> decoder) {
        c2sIdMap.put(clazz, id);
        c2sDecoderMap.put(id, decoder);
    }

    /**
     * The registered class <b>must</b> have a constructor accepting a {@link PacketByteBuf} or else an error will be thrown.
     * The visibility of this constructor does not matter.
     */
    public <T extends C2SPacket> void registerC2SPacket(Class<T> clazz, int id) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(PacketByteBuf.class);
            ctor.setAccessible(true);
            registerC2SPacket(clazz, id, buf -> {
                try {
                    return ctor.newInstance(buf);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (Exception exception) {
            LOGGER.error("Could not register C2S packet for channel '" + channelName + "' with id " + id, exception);
        }
    }

    public <T extends S2CPacket> void registerS2CPacket(Class<T> clazz, int id, Function<PacketByteBuf, T> decoder) {
        s2cIdMap.put(clazz, id);
        s2cDecoderMap.put(id, decoder);
    }

    /**
     * The registered class <b>must</b> have a constructor accepting a {@link PacketByteBuf} or else an error will be thrown.
     * The visibility of this constructor does not matter.
     */
    public <T extends S2CPacket> void registerS2CPacket(Class<T> clazz, int id) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(PacketByteBuf.class);
            constructor.setAccessible(true);
            registerS2CPacket(clazz, id, buf -> {
                try {
                    return constructor.newInstance(buf);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (Exception exception) {
            LOGGER.error("Could not register S2C packet for channel '" + channelName + "' with id " + id, exception);
        }
    }

    @Nullable
    public PacketByteBuf createBuf(C2SPacket packet) {
        Integer id = c2sIdMap.get(packet.getClass());
        if (id == null) {
            LOGGER.error("Could not get id for C2S packet '" + packet + "' in channel '" + channelName + "'");
            return null;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(id);
        packet.encode(buf);
        return buf;
    }

    @Nullable
    public PacketByteBuf createBuf(S2CPacket packet) {
        Integer id = s2cIdMap.get(packet.getClass());
        if (id == null) {
            LOGGER.error("Could not get id for S2C packet '" + packet + "' in channel '" + channelName + "'");
            return null;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(id);
        packet.encode(buf);
        return buf;
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public Packet<?> createVanillaPacket(C2SPacket packet) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return null;
        return ClientPlayNetworking.createC2SPacket(channelName, buf);
    }

    @Nullable
    public Packet<?> createVanillaPacket(S2CPacket packet) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return null;
        return ServerPlayNetworking.createS2CPacket(channelName, buf);
    }

    public void send(C2SPacket packet, PacketSender packetSender) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return;
        packetSender.sendPacket(channelName, buf);
    }

    public void send(S2CPacket packet, PacketSender packetSender) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return;
        packetSender.sendPacket(channelName, buf);
    }

    @Environment(EnvType.CLIENT)
    public void sendToServer(C2SPacket packet) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return;
        ClientPlayNetworking.send(channelName, buf);
    }

    public void sendToClient(S2CPacket packet, ServerPlayerEntity player) {
        PacketByteBuf buf = createBuf(packet);
        if (buf == null) return;
        ServerPlayNetworking.send(player, channelName, buf);
    }

    public void sendToClients(S2CPacket packet, Iterable<ServerPlayerEntity> players) {
        Packet<?> vanillaPacket = createVanillaPacket(packet);
        if (vanillaPacket == null) return;
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.getSender(player).sendPacket(vanillaPacket);
        }
    }

    public void sendToClientsInServer(S2CPacket packet, MinecraftServer server) {
        sendToClients(packet, PlayerLookup.all(server));
    }

    public void sendToClientsInCurrentServer(S2CPacket packet) {
        sendToClientsInServer(packet, ServerLifecycleHooks.getCurrentServer());
    }

    public void sendToClientsInWorld(S2CPacket packet, ServerWorld world) {
        sendToClients(packet, PlayerLookup.world(world));
    }

    public void sendToClientsTracking(S2CPacket packet, ServerWorld world, BlockPos pos) {
        sendToClients(packet, PlayerLookup.tracking(world, pos));
    }

    public void sendToClientsTracking(S2CPacket packet, ServerWorld world, ChunkPos pos) {
        sendToClients(packet, PlayerLookup.tracking(world, pos));
    }

    public void sendToClientsTracking(S2CPacket packet, Entity entity) {
        sendToClients(packet, PlayerLookup.tracking(entity));
    }

    public void sendToClientsTracking(S2CPacket packet, BlockEntity blockEntity) {
        sendToClients(packet, PlayerLookup.tracking(blockEntity));
    }

    public void sendToClientsTrackingAndSelf(S2CPacket packet, Entity entity) {
        Collection<ServerPlayerEntity> clients = PlayerLookup.tracking(entity);
        if (entity instanceof ServerPlayerEntity player && !clients.contains(player)) {
            clients = new ArrayList<>(clients);
            clients.add(player);
        }
        sendToClients(packet, clients);
    }

    public void sendToClientsAround(S2CPacket packet, ServerWorld world, Vec3d pos, double radius) {
        sendToClients(packet, PlayerLookup.around(world, pos, radius));
    }

    public void sendToClientsAround(S2CPacket packet, ServerWorld world, Vec3i pos, double radius) {
        sendToClients(packet, PlayerLookup.around(world, pos, radius));
    }

    public Identifier getChannelName() {
        return channelName;
    }

    private class C2SHandler implements ServerPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            int id = buf.readVarInt();
            C2SPacket packet;
            try {
                packet = c2sDecoderMap.get(id).apply(buf);
            } catch (Exception exception) {
                LOGGER.error("Could not create C2S packet in channel '" + channelName + "' with id " + id, exception);
                return;
            }
            packet.handle(server, player, handler, responseSender, SimpleChannel.this);
        }
    }

    @Environment(EnvType.CLIENT)
    private class S2CHandler implements ClientPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            int id = buf.readVarInt();
            S2CPacket packet;
            try {
                packet = s2cDecoderMap.get(id).apply(buf);
            } catch (Exception exception) {
                LOGGER.error("Could not create S2C packet in channel '" + channelName + "' with id " + id, exception);
                return;
            }
            packet.handle(client, handler, responseSender, SimpleChannel.this);
        }
    }
}
