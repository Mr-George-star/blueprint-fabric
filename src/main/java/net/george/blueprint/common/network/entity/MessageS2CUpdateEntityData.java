package net.george.blueprint.common.network.entity;

import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.common.world.storage.tracking.IDataManager;
import net.george.blueprint.common.world.storage.tracking.TrackedDataManager;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

import java.util.Set;

/**
 * The message for updating data about a {@link IDataManager} on clients.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class MessageS2CUpdateEntityData implements S2CPacket {
    private final int entityId;
    private final Set<IDataManager.DataEntry<?>> entries;

    public MessageS2CUpdateEntityData(int entityId, Set<IDataManager.DataEntry<?>> entries) {
        this.entityId = entityId;
        this.entries = entries;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.entries.size());
        this.entries.forEach(entry -> entry.write(buf));
    }

    public static MessageS2CUpdateEntityData decode(PacketByteBuf buf) {
        int entityId = buf.readInt();
        int size = buf.readInt();
        Set<IDataManager.DataEntry<?>> entries = Sets.newHashSet();
        for (int i = 0; i < size; i++) {
            entries.add(IDataManager.DataEntry.read(buf));
        }
        return new MessageS2CUpdateEntityData(entityId, entries);
    }

    @Override
    public void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            Entity entity = ClientInfo.getClientPlayerWorld().getEntityById(this.entityId);
            if (entity instanceof IDataManager) {
                this.entries.forEach(dataEntry -> setTrackedValue(entity, dataEntry));
            }
        });
    }

    private static <T> void setTrackedValue(Entity entity, IDataManager.DataEntry<T> entry) {
        TrackedDataManager.INSTANCE.setValue(entity, entry.getTrackedData(), entry.getValue());
    }
}
