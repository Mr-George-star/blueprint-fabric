package net.george.blueprint.common.entity;

import net.george.blueprint.common.network.NetworkHooks;
import net.george.blueprint.common.network.entity.SpawnEntityS2CPacket;
import net.george.blueprint.core.registry.BlueprintEntityTypes;
import net.george.blueprint.core.registry.BoatRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/**
 * A {@link BoatEntity} extension responsible for Blueprint's boats.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public class BlueprintBoat extends BoatEntity {
    private static final TrackedData<String> BOAT_TYPE = DataTracker.registerData(BlueprintBoat.class, TrackedDataHandlerRegistry.STRING);

    public BlueprintBoat(EntityType<? extends BoatEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    public BlueprintBoat(World world, double x, double y, double z) {
        this(BlueprintEntityTypes.BOAT.get(), world);
        this.setPos(x, y, z);
        this.setVelocity(Vec3d.ZERO);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    public BlueprintBoat(SpawnEntityS2CPacket spawnEntity, World level) {
        this(BlueprintEntityTypes.BOAT.get(), level);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BOAT_TYPE, "minecraft:oak");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Type", BoatRegistry.getNameForData(this.getBoat()));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Type", NbtElement.STRING_TYPE)) {
            String type = nbt.getString("Type");
            BoatRegistry.BoatData data = BoatRegistry.getDataForBoat(type);
            if (data != null) {
                this.setBoat(BoatRegistry.getNameForData(data));
            }
            else{
                this.setBoat(BoatRegistry.getBaseBoatName());
            }
        } else {
            this.setBoat(BoatRegistry.getBaseBoatName());
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        this.fallVelocity = this.getVelocity().y;
        if (!this.hasVehicle()) {
            if (onGround) {
                if (this.fallDistance > 3.0F) {
                    if (this.location != BlueprintBoat.Location.ON_LAND) {
                        this.fallDistance = 0.0F;
                        return;
                    }

                    this.handleFallDamage(this.fallDistance, 1.0F, DamageSource.FALL);
                    if (!this.world.isClient && this.isAlive()) {
                        this.kill();
                        if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                            for (int i = 0; i < 3; ++i) {
                                this.dropItem(this.getBoat().getPlankItem());
                            }

                            for (int j = 0; j < 2; ++j) {
                                this.dropItem(Items.STICK);
                            }
                        }
                    }
                }

                this.fallDistance = 0.0F;
            } else if (!this.world.getFluidState((new BlockPos(this.getPos())).down()).isIn(FluidTags.WATER) && heightDifference < 0.0D) {
                this.fallDistance = (float) ((double) this.fallDistance - heightDifference);
            }
        }
    }

    @Override
    public Item asItem() {
        return this.getBoat().getBoatItem();
    }

    public BoatRegistry.BoatData getBoat() {
        return BoatRegistry.getDataForBoat(this.dataTracker.get(BOAT_TYPE));
    }

    public void setBoat(String boat) {
        this.dataTracker.set(BOAT_TYPE, boat);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
