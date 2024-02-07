package net.george.blueprint.core.util.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.common.item.BlueprintBoatItem;
import net.george.blueprint.common.item.FuelItem;
import net.george.blueprint.core.api.registry.DeferredRegister;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.george.blueprint.core.registry.BoatRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A basic {@link AbstractSubRegistryHelper} for items.
 * <p>This contains some useful registering methods for items.</p>
 *
 * @author SmellyModder (Luke Tonon)
 * @see AbstractSubRegistryHelper
 */
@SuppressWarnings("unused")
public class ItemSubRegistryHelper extends AbstractSubRegistryHelper<Item> {
    public ItemSubRegistryHelper(RegistryHelper parent, DeferredRegister<Item> deferredRegister) {
        super(parent, deferredRegister);
    }

    public ItemSubRegistryHelper(RegistryHelper parent) {
        super(parent, DeferredRegister.of(Registry.ITEM, parent.getModId()));
    }

    /**
     * Creates and registers a {@link WallStandingBlockItem}.
     *
     * @param floorBlock The floor {@link Block}.
     * @param wallBlock  The wall {@link Block}.
     * @param itemGroup  The {@link ItemGroup} for the {@link WallStandingBlockItem}.
     * @return The created {@link WallStandingBlockItem}.
     * @see WallStandingBlockItem
     */
    public static BlockItem createStandingAndWallBlockItem(Block floorBlock, Block wallBlock, ItemGroup itemGroup) {
        return new WallStandingBlockItem(floorBlock, wallBlock, new Item.Settings().group(itemGroup));
    }

    /**
     * Creates and registers a {@link TallBlockItem}.
     *
     * @param blockForInput The {@link Block} for the item.
     * @param itemGroup     The {@link ItemGroup} for the {@link TallBlockItem}.
     * @return The created {@link TallBlockItem}.
     * @see TallBlockItem
     */
    public static BlockItem createDoubleHighBlockItem(Block blockForInput, ItemGroup itemGroup) {
        return new TallBlockItem(blockForInput, new Item.Settings().group(itemGroup));
    }

    /**
     * Creates a {@link FuelItem}.
     *
     * @param burnTime  How long the item will burn (measured in ticks).
     * @param itemGroup The {@link ItemGroup} for the {@link FuelItem}.
     * @return The created {@link FuelItem}.
     */
    public static FuelItem createFuelItem(int burnTime, ItemGroup itemGroup) {
        return new FuelItem(burnTime, new Item.Settings().group(itemGroup));
    }

    /**
     * Creates a {@link BlockItem} with a specified {@link Block} and {@link ItemGroup}.
     *
     * @param blockForInput The {@link Block} for the {@link BlockItem}.
     * @param itemGroup     The {@link ItemGroup} for the {@link BlockItem}, null to have it be in no group.
     * @return The BlockItem.
     */
    public static BlockItem createSimpleBlockItem(Block blockForInput, @Nullable ItemGroup itemGroup) {
        return new BlockItem(blockForInput, new Item.Settings().group(itemGroup));
    }

    /**
     * Creates a simple {@link Item.Settings} with a stack size and {@link ItemGroup}.
     *
     * @param stackSize The item's max stack size.
     * @param itemGroup The item's CreativeModeTab.
     * @return The simple {@link Item.Settings}.
     */
    public static Item.Settings createSimpleItemProperty(int stackSize, ItemGroup itemGroup) {
        return new Item.Settings().group(itemGroup).maxCount(stackSize);
    }

    /**
     * Registers an {@link Item}.
     *
     * @param name     The name for the item.
     * @param supplier A {@link Supplier} containing the {@link Item}.
     * @return A {@link RegistryObject} containing the {@link Item}.
     */
    public <I extends Item> RegistryObject<I> createItem(String name, Supplier<? extends I> supplier) {
        return this.deferredRegister.register(name, supplier);
    }

    /**
     * Creates and registers a compat {@link Item}.
     *
     * @param modId      The mod id of the mod this item is compatible for, set to "indev" for dev tests.
     * @param name       The name for the item.
     * @param settings The item's properties.
     * @param group      The {@link ItemGroup} for the {@link Item}.
     * @return A {@link RegistryObject} containing the {@link Item}.
     */
    public RegistryObject<Item> createCompatItem(String modId, String name, Item.Settings settings, ItemGroup group) {
        return this.deferredRegister.register(name, () -> new Item(settings.group(FabricLoader.getInstance().isModLoaded(modId) || Objects.equals(modId, "indev") ? group : null)));
    }

    /**
     * Creates and registers a compat {@link Item}.
     *
     * @param name       The name for the item.
     * @param settings The item's properties.
     * @param group      The {@link ItemGroup} for the {@link Item}.
     * @param modIds     The mod ids of the mods this block is compatible for.
     * @return A {@link RegistryObject} containing the {@link Item}.
     */
    public RegistryObject<Item> createCompatItem(String name, Item.Settings settings, ItemGroup group, String... modIds) {
        return this.deferredRegister.register(name, () -> new Item(settings.group(areModsLoaded(modIds) ? group : null)));
    }

    /**
     * Creates and registers a {@link SpawnEggItem}.
     *
     * @param entityName     The name of the entity this spawn egg spawns.
     * @param supplier       The supplied {@link EntityType}.
     * @param primaryColor   The egg's primary color.
     * @param secondaryColor The egg's secondary color.
     * @return A {@link RegistryObject} containing the {@link SpawnEggItem}.
     * @see SpawnEggItem
     */
    public RegistryObject<SpawnEggItem> createSpawnEggItem(String entityName, EntityType<? extends MobEntity> supplier, int primaryColor, int secondaryColor) {
        return this.deferredRegister.register(entityName + "_spawn_egg", () -> new SpawnEggItem(supplier, primaryColor, secondaryColor, new Item.Settings().group(ItemGroup.MISC)));
    }

    /**
     * Creates and registers a {@link BlueprintBoatItem} and boat type.
     *
     * @param wood  The name of the wood, e.g. "oak".
     * @param block The {@link Block} for the boat to drop.
     */
    public RegistryObject<Item> createBoatItem(String wood, RegistryObject<Block> block) {
        String type = this.parent.getModId() + ":" + wood;
        RegistryObject<Item> boat = this.deferredRegister.register(wood + "_boat", () -> new BlueprintBoatItem(type, createSimpleItemProperty(1, ItemGroup.TRANSPORTATION)));
        BoatRegistry.registerBoat(type, boat, block);
        return boat;
    }
}
