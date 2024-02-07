package net.george.blueprint.core.api.config.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.config.ConfigTracker;
import net.george.blueprint.core.api.config.ModConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ConfigSync {
    static final Marker NETWORK = MarkerManager.getMarker("CONFIG-NETWORKING");
    public static final Marker FMLHSMARKER;
    public static final Logger LOGGER;
    public static final ConfigSync INSTANCE;
    public static final Identifier SYNC_CONFIGS_CHANNEL;
    public static final Identifier MODDED_CONNECTION_CHANNEL;
    private final ConfigTracker tracker;

    private ConfigSync(ConfigTracker tracker) {
        this.tracker = tracker;
    }

    public void init() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            List<Pair<String, PacketByteBuf>> pairs = this.syncConfigs();

            for (Pair<String, PacketByteBuf> stringPacketByteBufPair : pairs) {
                synchronizer.waitFor(server.submit(() ->
                        sender.sendPacket(SYNC_CONFIGS_CHANNEL, stringPacketByteBufPair.getValue())));
            }

            synchronizer.waitFor(server.submit(() -> sender.sendPacket(MODDED_CONNECTION_CHANNEL, PacketByteBufs.create())));
        });
        ServerLoginNetworking.registerGlobalReceiver(SYNC_CONFIGS_CHANNEL, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (understood) {
                String fileName = buf.readString(32767);
                LOGGER.debug(FMLHSMARKER, "Received acknowledgement for config sync for {} from client", fileName);
            }
        });
        ServerLoginNetworking.registerGlobalReceiver(MODDED_CONNECTION_CHANNEL, (server, handler, understood, buf, synchronizer, responseSender) ->
                LOGGER.debug(FMLHSMARKER, "Received acknowledgement for modded connection marker from client"));
    }

    private List<Pair<String, PacketByteBuf>> syncConfigs() {
        Map<String, byte[]> configData = (this.tracker.configSets().get(ModConfig.Type.SERVER)).stream()
                .collect(Collectors.toMap(ModConfig::getFileName, (config) -> {
            try {
                return Files.readAllBytes(config.getFullPath());
            } catch (IOException var2) {
                throw new RuntimeException(var2);
            }
        }));
        return configData.entrySet().stream().map((entry) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(entry.getKey());
            buf.writeByteArray(entry.getValue());
            return Pair.of("Config " + entry.getKey(), buf);
        }).collect(Collectors.toList());
    }

    static {
        FMLHSMARKER = MarkerManager.getMarker("CONFIG-HANDSHAKE").setParents(NETWORK);
        LOGGER = LogManager.getLogger();
        INSTANCE = new ConfigSync(ConfigTracker.INSTANCE);
        SYNC_CONFIGS_CHANNEL = new Identifier(Blueprint.MOD_ID, "sync_configs");
        MODDED_CONNECTION_CHANNEL = new Identifier(Blueprint.MOD_ID, "modded_connection");
    }
}
