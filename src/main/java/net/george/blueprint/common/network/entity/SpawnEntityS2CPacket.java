package net.george.blueprint.common.network.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.common.server.LogicalSidedProvider;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public class SpawnEntityS2CPacket implements S2CPacket {
    private final Entity entity;
    private final int typeId;
    private final int entityId;
    private final UUID uuid;
    private final double posX, posY, posZ;
    private final byte pitch, yaw, headYaw;
    private final int velocityX, velocityY, velocityZ;
    private final PacketByteBuf buf;

    public SpawnEntityS2CPacket(Entity entity) {
        this.entity = entity;
        this.typeId = Registry.ENTITY_TYPE.getRawId(entity.getType());
        this.entityId = entity.getId();
        this.uuid = entity.getUuid();
        this.posX = entity.getX();
        this.posY = entity.getY();
        this.posZ = entity.getZ();
        this.pitch = (byte) MathHelper.floor(entity.getPitch() * 256.0F / 360.0F);
        this.yaw = (byte) MathHelper.floor(entity.getYaw() * 256.0F / 360.0F);
        this.headYaw = (byte) (entity.getHeadYaw() * 256.0F / 360.0F);
        Vec3d vec3d = entity.getVelocity();
        double d1 = MathHelper.clamp(vec3d.x, -3.9D, 3.9D);
        double d2 = MathHelper.clamp(vec3d.y, -3.9D, 3.9D);
        double d3 = MathHelper.clamp(vec3d.z, -3.9D, 3.9D);
        this.velocityX = (int)(d1 * 8000.0D);
        this.velocityY = (int)(d2 * 8000.0D);
        this.velocityZ = (int)(d3 * 8000.0D);
        this.buf = null;
    }

    private SpawnEntityS2CPacket(int typeId, int entityId, UUID uuid, double posX, double posY, double posZ,
                                 byte pitch, byte yaw, byte headYaw, int velocityX, int velocityY, int velocityZ, PacketByteBuf buf) {
        this.entity = null;
        this.typeId = typeId;
        this.entityId = entityId;
        this.uuid = uuid;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.buf = buf;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeVarInt(this.typeId);
        buf.writeInt(this.entityId);
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeByte(this.pitch);
        buf.writeByte(this.yaw);
        buf.writeByte(this.headYaw);
        buf.writeShort(this.velocityX);
        buf.writeShort(this.velocityY);
        buf.writeShort(this.velocityZ);
    }

    public static SpawnEntityS2CPacket decode(PacketByteBuf buf) {
        return new SpawnEntityS2CPacket(buf.readVarInt(), buf.readInt(), new UUID(buf.readLong(), buf.readLong()),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readByte(), buf.readByte(), buf.readByte(),
                buf.readShort(), buf.readShort(), buf.readShort(), buf);
    }

    @Override
    public void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            EntityType<?> type;
            try {
                type = Registry.ENTITY_TYPE.get(this.typeId);
            } catch (Throwable exception) {
                throw new RuntimeException(String.format(Locale.ENGLISH, "Could not spawn entity (id %d) with unknown type at (%f, %f, %f)", this.entityId, this.posX, this.posY, this.posZ));
            }
            EntityType<?> finalType = type;

            Optional<World> world = LogicalSidedProvider.CLIENT_WORLD.get(EnvType.CLIENT);
            Entity targetEntity = world.map(finalType::create).orElse(null);
            if (targetEntity == null) {
                return;
            }

            targetEntity.updateTrackedPosition(this.posX, this.posY, this.posZ);
            targetEntity.updatePositionAndAngles(this.posX, this.posY, this.posZ, (this.yaw * 360) / 256.0F, (this.pitch * 360) / 256.0F);
            targetEntity.setHeadYaw((this.headYaw * 360) / 256.0F);
            targetEntity.setBodyYaw((this.headYaw * 360) / 256.0F);

            targetEntity.setId(this.entityId);
            targetEntity.setUuid(this.uuid);
            world.filter(ClientWorld.class::isInstance).ifPresent(world2 -> ((ClientWorld)world2).addEntity(this.entityId, targetEntity));
            targetEntity.setVelocityClient(this.velocityX / 8000.0, this.velocityY / 8000.0, this.velocityZ / 8000.0);
        });
    }

    public Entity getEntity() {
        return this.entity;
    }

    public int getTypeId() {
        return this.typeId;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public byte getYaw() {
        return this.yaw;
    }

    public byte getHeadYaw() {
        return this.headYaw;
    }

    public int getVelocityX() {
        return this.velocityX;
    }

    public int getVelocityY() {
        return this.velocityY;
    }

    public int getVelocityZ() {
        return this.velocityZ;
    }

    public PacketByteBuf getAdditionalData() {
        return this.buf;
    }
}
