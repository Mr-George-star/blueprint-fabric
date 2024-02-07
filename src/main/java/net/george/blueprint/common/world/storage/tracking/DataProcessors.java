package net.george.blueprint.common.world.storage.tracking;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * This class contains the built-in {@link IDataProcessor}s.
 * Use these fields for some primitive types or basic types.
 * <p>Feel free to make PRs to add more of these!</p>
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class DataProcessors {
    public static final IDataProcessor<Boolean> BOOLEAN = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Boolean bool) {
            NbtCompound compound = new NbtCompound();
            compound.putBoolean("Boolean", bool);
            return compound;
        }

        @Override
        public Boolean read(NbtCompound nbt) {
            return nbt.getBoolean("Boolean");
        }
    };

    public static final IDataProcessor<Byte> BYTE = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Byte abyte) {
            NbtCompound compound = new NbtCompound();
            compound.putByte("Byte", abyte);
            return compound;
        }

        @Override
        public Byte read(NbtCompound nbt) {
            return nbt.getByte("Byte");
        }
    };

    public static final IDataProcessor<Short> SHORT = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Short ashort) {
            NbtCompound compound = new NbtCompound();
            compound.putShort("Short", ashort);
            return compound;
        }

        @Override
        public Short read(NbtCompound nbt) {
            return nbt.getShort("Short");
        }
    };

    public static final IDataProcessor<Integer> INT = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Integer integer) {
            NbtCompound compound = new NbtCompound();
            compound.putInt("Integer", integer);
            return compound;
        }

        @Override
        public Integer read(NbtCompound nbt) {
            return nbt.getInt("Integer");
        }
    };

    public static final IDataProcessor<Long> LONG = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Long along) {
            NbtCompound compound = new NbtCompound();
            compound.putLong("Long", along);
            return compound;
        }

        @Override
        public Long read(NbtCompound nbt) {
            return nbt.getLong("Long");
        }
    };

    public static final IDataProcessor<Float> FLOAT = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Float afloat) {
            NbtCompound compound = new NbtCompound();
            compound.putFloat("Float", afloat);
            return compound;
        }

        @Override
        public Float read(NbtCompound nbt) {
            return nbt.getFloat("Float");
        }
    };

    public static final IDataProcessor<Double> DOUBLE = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Double aDouble) {
            NbtCompound compound = new NbtCompound();
            compound.putDouble("Double", aDouble);
            return compound;
        }

        @Override
        public Double read(NbtCompound nbt) {
            return nbt.getDouble("Double");
        }
    };

    public static final IDataProcessor<String> STRING = new IDataProcessor<>() {
        @Override
        public NbtCompound write(String aString) {
            NbtCompound compound = new NbtCompound();
            compound.putString("String", aString);
            return compound;
        }

        @Override
        public String read(NbtCompound nbt) {
            return nbt.getString("String");
        }
    };

    public static final IDataProcessor<BlockPos> POS = new IDataProcessor<>() {
        @Override
        public NbtCompound write(BlockPos pos) {
            NbtCompound compound = new NbtCompound();
            compound.putLong("Pos", pos.asLong());
            return compound;
        }

        @Override
        public BlockPos read(NbtCompound compound) {
            return BlockPos.fromLong(compound.getLong("Pos"));
        }
    };

    public static final IDataProcessor<java.util.UUID> UUID = new IDataProcessor<>() {
        @Override
        public NbtCompound write(UUID uuid) {
            NbtCompound compound = new NbtCompound();
            compound.putUuid("UUID", uuid);
            return compound;
        }

        @Override
        public UUID read(NbtCompound compound) {
            return compound.getUuid("UUID");
        }
    };

    public static final IDataProcessor<NbtCompound> COMPOUND = new IDataProcessor<>() {
        @Override
        public NbtCompound write(NbtCompound compound) {
            return compound;
        }

        @Override
        public NbtCompound read(NbtCompound compound) {
            return compound;
        }
    };

    public static final IDataProcessor<ItemStack> STACK = new IDataProcessor<>() {
        @Override
        public NbtCompound write(ItemStack stack) {
            return stack.writeNbt(new NbtCompound());
        }

        @Override
        public ItemStack read(NbtCompound compound) {
            return ItemStack.fromNbt(compound);
        }
    };

    public static final IDataProcessor<Identifier> RESOURCE_LOCATION = new IDataProcessor<>() {
        @Override
        public NbtCompound write(Identifier resourceLocation) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Identifier", resourceLocation.toString());
            return compound;
        }

        @Override
        public Identifier read(NbtCompound compound) {
            return new Identifier(compound.getString("Identifier"));
        }
    };
}
