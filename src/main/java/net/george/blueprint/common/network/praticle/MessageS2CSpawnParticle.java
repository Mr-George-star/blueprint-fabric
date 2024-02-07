package net.george.blueprint.common.network.praticle;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.george.blueprint.client.ClientInfo;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.network.packet.S2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Message for telling the client to spawn a particle.
 *
 * @author SmellyModder(Luke Tonon)
 */
public final class MessageS2CSpawnParticle implements S2CPacket {
    public String particleName;
    public double posX, posY, posZ;
    public double motionX, motionY, motionZ;

    public MessageS2CSpawnParticle(String particleName, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        this.particleName = particleName;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeString(this.particleName);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeDouble(this.motionX);
        buf.writeDouble(this.motionY);
        buf.writeDouble(this.motionZ);
    }

    public static MessageS2CSpawnParticle decode(PacketByteBuf buf) {
        String particleName = buf.readString();
        double posX = buf.readDouble();
        double posY = buf.readDouble();
        double posZ = buf.readDouble();
        double motionX = buf.readDouble();
        double motionY = buf.readDouble();
        double motionZ = buf.readDouble();
        return new MessageS2CSpawnParticle(particleName, posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void handle(MinecraftClient client, ClientPlayNetworkHandler listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            World level = ClientInfo.getClientPlayerWorld();
            DefaultParticleType particleType = (DefaultParticleType) Registry.PARTICLE_TYPE.get(new Identifier(this.particleName));

            if (particleType != null) {
                level.addParticle(particleType, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
            }
        });
    }
}
