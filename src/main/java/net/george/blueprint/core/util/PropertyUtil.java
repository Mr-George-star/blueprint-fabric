package net.george.blueprint.core.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.ApiStatus;

/**
 * A class containing template {@link AbstractBlock.Settings} and {@link Item.Settings}
 *
 * @author bageldotjpg
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class PropertyUtil {
    public static final Item.Settings TOOL;
    public static final Item.Settings MISC_TOOL;
    public static final AbstractBlock.Settings FLOWER;
    public static final AbstractBlock.Settings SAPLING;
    public static final AbstractBlock.Settings LADDER;
    public static final AbstractBlock.Settings FLOWER_POT;

    public PropertyUtil() {
    }

    public static Item.Settings food(FoodComponent food) {
        return new Item.Settings().food(food).group(ItemGroup.FOOD);
    }

    public static AbstractBlock.Settings thatch(DyeColor color, BlockSoundGroup soundGroup) {
        return AbstractBlock.Settings.of(Material.SOLID_ORGANIC, color).strength(0.5F).sounds(soundGroup).nonOpaque();
    }

    public static boolean never(BlockState state, BlockView getter, BlockPos pos) {
        return false;
    }

    public static boolean never(BlockState state, BlockView getter, BlockPos pos, EntityType<?> entity) {
        return false;
    }

    public static boolean always(BlockState state, BlockView getter, BlockPos pos) {
        return true;
    }

    public static boolean always(BlockState state, BlockView getter, BlockPos pos, EntityType<?> entity) {
        return true;
    }

    public static boolean ocelotOrParrot(BlockState state, BlockView reader, BlockPos pos, EntityType<?> entity) {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    static {
        TOOL = new Item.Settings().maxCount(1).group(ItemGroup.TOOLS);
        MISC_TOOL = new Item.Settings().maxCount(1).group(ItemGroup.MISC);
        FLOWER = AbstractBlock.Settings.of(Material.PLANT).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS);
        SAPLING = AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS);
        LADDER = AbstractBlock.Settings.of(Material.DECORATION).strength(0.4F).sounds(BlockSoundGroup.LADDER).nonOpaque();
        FLOWER_POT = AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().nonOpaque();
    }

    public record WoodSetSettings(MapColor woodColor, Material material, BlockSoundGroup sound, BlockSoundGroup logSound, BlockSoundGroup leavesSound) {
        public static Builder builder(MapColor woodColor) {
            return new Builder(woodColor);
        }

        public AbstractBlock.Settings planks() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(2.0F, 3.0F).sounds(this.sound);
        }

        public AbstractBlock.Settings log() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(2.0F).sounds(this.logSound);
        }

        public AbstractBlock.Settings leaves() {
            return AbstractBlock.Settings.of(Material.LEAVES).strength(0.2F).ticksRandomly().sounds(this.leavesSound).nonOpaque().allowsSpawning(PropertyUtil::ocelotOrParrot).suffocates(PropertyUtil::never).blockVision(PropertyUtil::never);
        }

        public AbstractBlock.Settings pressurePlate() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).noCollision().strength(0.5F).sounds(this.sound);
        }

        public AbstractBlock.Settings trapdoor() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(3.0F).sounds(this.sound).nonOpaque().allowsSpawning(PropertyUtil::never);
        }

        public AbstractBlock.Settings button() {
            return AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(this.sound);
        }

        public AbstractBlock.Settings door() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(3.0F).sounds(this.sound).nonOpaque();
        }

        public AbstractBlock.Settings beehive() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(0.6F).sounds(this.sound);
        }

        public AbstractBlock.Settings bookshelf() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(1.5F).sounds(this.sound);
        }

        public AbstractBlock.Settings ladder() {
            return PropertyUtil.LADDER;
        }

        public AbstractBlock.Settings chest() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(2.5F).sounds(this.sound);
        }

        public AbstractBlock.Settings leafPile() {
            return AbstractBlock.Settings.of(Material.REPLACEABLE_PLANT).noCollision().strength(0.2F).sounds(this.leavesSound);
        }

        public AbstractBlock.Settings leafCarpet() {
            return AbstractBlock.Settings.of(Material.DECORATION).strength(0.0F).sounds(this.leavesSound).nonOpaque();
        }

        public AbstractBlock.Settings post() {
            return AbstractBlock.Settings.of(this.material, this.woodColor).strength(2.0F, 3.0F).sounds(this.logSound);
        }

        public MapColor woodColor() {
            return this.woodColor;
        }

        public Material material() {
            return this.material;
        }

        public BlockSoundGroup sound() {
            return this.sound;
        }

        public BlockSoundGroup logSound() {
            return this.logSound;
        }

        public BlockSoundGroup leavesSound() {
            return this.leavesSound;
        }

        public static final class Builder {
            private MapColor woodColor;
            private Material material;
            private BlockSoundGroup sound;
            private BlockSoundGroup logSound;
            private BlockSoundGroup leavesSound;

            private Builder(MapColor woodColor) {
                this.material = Material.WOOD;
                this.sound = BlockSoundGroup.WOOD;
                this.logSound = BlockSoundGroup.WOOD;
                this.leavesSound = BlockSoundGroup.GRASS;
                this.woodColor = woodColor;
            }

            public Builder material(Material material) {
                this.material = material;
                return this;
            }

            public Builder woodColor(MapColor woodColor) {
                this.woodColor = woodColor;
                return this;
            }

            public Builder sound(BlockSoundGroup soundType) {
                this.sound = soundType;
                return this;
            }

            public Builder logSound(BlockSoundGroup soundType) {
                this.logSound = soundType;
                return this;
            }

            public Builder leavesSound(BlockSoundGroup soundType) {
                this.leavesSound = soundType;
                return this;
            }

            public WoodSetSettings build() {
                return new WoodSetSettings(this.woodColor, this.material, this.sound, this.logSound, this.leavesSound);
            }
        }
    }
}
