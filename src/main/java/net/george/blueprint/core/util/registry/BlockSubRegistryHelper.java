package net.george.blueprint.core.util.registry;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.client.ChestManager;
import net.george.blueprint.common.block.chest.BlueprintChestBlock;
import net.george.blueprint.common.block.chest.BlueprintTrappedChestBlock;
import net.george.blueprint.common.block.sign.BlueprintStandingSignBlock;
import net.george.blueprint.common.block.sign.BlueprintWallSignBlock;
import net.george.blueprint.common.item.FuelBlockItem;
import net.george.blueprint.common.item.InjectedBlockItem;
import net.george.blueprint.core.api.SignManager;
import net.george.blueprint.core.api.registry.DeferredRegister;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.george.blueprint.core.mixin.accessor.SignTypeAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;
import net.minecraft.util.SignType;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A basic {@link AbstractSubRegistryHelper} for blocks. This contains some useful registering methods for blocks.
 *
 * @author SmellyModder (Luke Tonon)
 * @see AbstractSubRegistryHelper
 */
@SuppressWarnings("unused")
public class BlockSubRegistryHelper extends AbstractSubRegistryHelper<Block> {
    protected final DeferredRegister<Item> itemRegister;

    public BlockSubRegistryHelper(RegistryHelper parent) {
        this(parent, parent.getSubHelper(Registry.ITEM).getDeferredRegister(), DeferredRegister.of(Registry.BLOCK, parent.getModId()));
    }

    public BlockSubRegistryHelper(RegistryHelper parent, ISubRegistryHelper<Item> itemHelper) {
        this(parent, itemHelper.getDeferredRegister(), DeferredRegister.of(Registry.BLOCK, parent.getModId()));
    }

    public BlockSubRegistryHelper(RegistryHelper parent, DeferredRegister<Item> itemRegister, DeferredRegister<Block> deferredRegister) {
        super(parent, deferredRegister);
        this.itemRegister = itemRegister;
    }

    /**
     * Creates and registers a {@link Block} with a {@link BlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createBlock(String name, Supplier<? extends B> supplier, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new BlockItem(block.get(), new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers a {@link Block} with a {@link BlockItem} with custom {@link Item.Settings}.
     *
     * @param name       The block's name.
     * @param supplier   The supplied {@link Block}.
     * @param settings The {@link Item.Settings} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createBlock(String name, Supplier<? extends B> supplier, Item.Settings settings) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new BlockItem(block.get(), settings));
        return block;
    }

    /**
     * Creates and registers a {@link Block} with a specified {@link BlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param item     The {@link BlockItem} for this {@link Block}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createBlockWithItem(String name, Supplier<? extends B> supplier, Supplier<BlockItem> item) {
        this.itemRegister.register(name, item);
        return this.deferredRegister.register(name, supplier);
    }

    /**
     * Creates and registers a {@link Block} with no {@link BlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied Block.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createBlockNoItem(String name, Supplier<? extends B> supplier) {
        return this.deferredRegister.register(name, supplier);
    }

    /**
     * Creates and registers a {@link Block} with its {@link BlockItem} that can be used as fuel.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param burnTime How long the item will burn (measured in ticks).
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createFuelBlock(String name, Supplier<? extends B> supplier, int burnTime, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new FuelBlockItem(block.get(), burnTime, new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers a {@link Block} with an {@link InjectedBlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param group    The {@link ItemGroup} for the {@link InjectedBlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createInjectedBlock(String name, Item followItem, Supplier<? extends B> supplier, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new InjectedBlockItem(followItem, block.get(), new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers a {@link Block} with a {@link TallBlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param group    The {@link ItemGroup} for the {@link TallBlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     * @see TallBlockItem
     */
    public <B extends Block> RegistryObject<B> createDoubleHighBlock(String name, Supplier<? extends B> supplier, ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new TallBlockItem(block.get(), new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers {@link Block} with a {@link WallStandingBlockItem}.
     *
     * @param name         The block's name.
     * @param supplier     The supplied floor {@link Block}.
     * @param wallSupplier The supplied wall {@link Block}.
     * @param group        The {@link ItemGroup} for the {@link WallStandingBlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     * @see WallStandingBlockItem
     */
    public <B extends Block> RegistryObject<B> createStandingAndWallBlock(String name, Supplier<? extends B> supplier, Supplier<? extends B> wallSupplier, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new WallStandingBlockItem(block.get(), wallSupplier.get(), new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers {@link Block} with a {@link BlockItem} that has {@link Rarity}.
     *
     * @param name   The block's name.
     * @param rarity The {@link Rarity} of the {@link BlockItem}.
     * @param group  The {@link ItemGroup} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createRareBlock(String name, Supplier<? extends B> supplier, Rarity rarity, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () -> new BlockItem(block.get(), new Item.Settings().rarity(rarity).group(group)));
        return block;
    }

    /**
     * Creates and registers {@link BlueprintChestBlock} with a {@link FuelBlockItem}.
     *
     * @param name       The name for this {@link BlueprintChestBlock}.
     * @param settings The properties for this {@link BlueprintChestBlock}.
     * @param group      The CreativeModeTab for the BlockItem.
     * @return A {@link RegistryObject} containing the created {@link BlueprintChestBlock}.
     */
    public RegistryObject<BlueprintChestBlock> createChestBlock(String name, AbstractBlock.Settings settings, @Nullable ItemGroup group) {
        String modId = this.parent.getModId();
        RegistryObject<BlueprintChestBlock> block = this.deferredRegister.register(name + "_chest", () ->
                new BlueprintChestBlock(modId + ":" + name, settings));
        ChestManager.putChestInfo(modId, name, false);
        this.itemRegister.register(name + "_chest", () ->
                new FuelBlockItem(block.get(), 300, new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers {@link BlueprintTrappedChestBlock} with a {@link FuelBlockItem}.
     *
     * @param name       The name for this {@link BlueprintTrappedChestBlock}.
     * @param settings The properties for this {@link BlueprintTrappedChestBlock}.
     * @param group      The CreativeModeTab for the BlockItem.
     * @return A {@link RegistryObject} containing the created {@link BlueprintTrappedChestBlock}.
     */
    public RegistryObject<BlueprintTrappedChestBlock> createTrappedChestBlock(String name, AbstractBlock.Settings settings, @Nullable ItemGroup group) {
        String modId = this.parent.getModId();
        RegistryObject<BlueprintTrappedChestBlock> block = this.deferredRegister.register(name + "_trapped_chest", () ->
                new BlueprintTrappedChestBlock(modId + ":" + name + "_trapped", settings));
        ChestManager.putChestInfo(modId, name, true);
        this.itemRegister.register(name + "_trapped_chest", () ->
                new FuelBlockItem(block.get(), 300, new Item.Settings().group(group)));
        return block;
    }

    /**
     * Creates and registers a {@link BlueprintStandingSignBlock} and a {@link BlueprintWallSignBlock} with a {@link SignItem}.
     *
     * @param name  The name for the sign blocks.
     * @param color The {@link MapColor} for the sign blocks.
     * @return A {@link Pair} containing {@link RegistryObject}s of the {@link BlueprintStandingSignBlock} and the {@link BlueprintWallSignBlock}.
     */
    public Pair<RegistryObject<BlueprintStandingSignBlock>, RegistryObject<BlueprintWallSignBlock>> createSignBlock(String name, MapColor color) {
        String var10000 = this.parent.getModId();
        SignType type = SignManager.registerWoodType(SignTypeAccessor.create(var10000 + ":" + name));
        RegistryObject<BlueprintStandingSignBlock> standing = this.deferredRegister.register(name + "_sign", () ->
                new BlueprintStandingSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollision().strength(1.0F).sounds(BlockSoundGroup.WOOD), type));
        RegistryObject<BlueprintWallSignBlock> wall = this.deferredRegister.register(name + "_wall_sign", () ->
                new BlueprintWallSignBlock(AbstractBlock.Settings.of(Material.WOOD, color).noCollision().strength(1.0F).sounds(BlockSoundGroup.WOOD).dropsLike(standing.get()), type));
        this.itemRegister.register(name + "_sign", () ->
                new SignItem(new Item.Settings().maxCount(16).group(ItemGroup.DECORATIONS), standing.get(), wall.get()));
        return Pair.of(standing, wall);
    }

    /**
     * Creates and registers a compat {@link Block}.
     *
     * @param modId    The mod id of the mod this block is compatible for, set to "indev" for dev tests.
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createCompatBlock(String modId, String name, Supplier<? extends B> supplier, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () ->
                new BlockItem(block.get(), new Item.Settings().group(!FabricLoader.getInstance().isModLoaded(modId) && !Objects.equals(modId, "indev") ? null : group)));
        return block;
    }

    /**
     * Creates and registers a compat {@link Block}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @param modIds   The mod ids of the mods this block is compatible for.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createCompatBlock(String name, Supplier<? extends B> supplier, @Nullable ItemGroup group, String... modIds) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () ->
                new BlockItem(block.get(), new Item.Settings().group(areModsLoaded(modIds) ? group : null)));
        return block;
    }

    /**
     * Creates and registers a compat {@link Block} with a {@link FuelBlockItem}.
     *
     * @param modId    The modId of the mod this block is compatible for, set to "indev" for dev tests.
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param burnTime How many ticks this fuel block should burn for.
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createCompatFuelBlock(String modId, String name, Supplier<? extends B> supplier, int burnTime, @Nullable ItemGroup group) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () ->
                new FuelBlockItem(block.get(), burnTime, new Item.Settings().group(!FabricLoader.getInstance().isModLoaded(modId) && !Objects.equals(modId, "indev") ? null : group)));
        return block;
    }

    /**
     * Creates and registers a compat {@link Block} with a {@link FuelBlockItem}.
     *
     * @param name     The block's name.
     * @param supplier The supplied {@link Block}.
     * @param burnTime How many ticks this fuel block should burn for.
     * @param group    The {@link ItemGroup} for the {@link BlockItem}.
     * @param modIds   The mod ids of the mods this block is compatible for.
     * @return A {@link RegistryObject} containing the created {@link Block}.
     */
    public <B extends Block> RegistryObject<B> createCompatFuelBlock(String name, Supplier<? extends B> supplier, int burnTime, @Nullable ItemGroup group, String... modIds) {
        RegistryObject<B> block = this.deferredRegister.register(name, supplier);
        this.itemRegister.register(name, () ->
                new FuelBlockItem(block.get(), burnTime, new Item.Settings().group(areModsLoaded(modIds) ? group : null)));
        return block;
    }

    /**
     * Creates and registers a {@link BlueprintChestBlock} and a {@link BlueprintTrappedChestBlock} with their {@link FuelBlockItem}s.
     *
     * @param name        The name for the chest blocks.
     * @param compatModId The mod id of the mod these chests are compatible for.
     * @param color       The {@link MapColor} for the chest blocks.
     * @return A {@link Pair} containing {@link RegistryObject}s of the {@link BlueprintChestBlock} and the {@link BlueprintTrappedChestBlock}.
     */
    public Pair<RegistryObject<BlueprintChestBlock>, RegistryObject<BlueprintTrappedChestBlock>> createCompatChestBlocks(String compatModId, String name, MapColor color) {
        boolean isModLoaded = FabricLoader.getInstance().isModLoaded(compatModId) || Objects.equals(compatModId, "indev");
        ItemGroup chestGroup = isModLoaded ? ItemGroup.DECORATIONS : null;
        ItemGroup trappedChestGroup = isModLoaded ? ItemGroup.REDSTONE : null;
        String modId = this.parent.getModId();
        String chestName = name + "_chest";
        String trappedChestName = name + "_trapped_chest";
        RegistryObject<BlueprintChestBlock> chest = this.deferredRegister.register(chestName, () ->
                new BlueprintChestBlock(modId + ":" + name, AbstractBlock.Settings.of(Material.WOOD, color).strength(2.5F).sounds(BlockSoundGroup.WOOD)));
        RegistryObject<BlueprintTrappedChestBlock> trappedChest = this.deferredRegister.register(trappedChestName, () ->
                new BlueprintTrappedChestBlock(modId + ":" + name + "_trapped", AbstractBlock.Settings.of(Material.WOOD, color).strength(2.5F).sounds(BlockSoundGroup.WOOD)));
        this.itemRegister.register(chestName, () ->
                new FuelBlockItem(chest.get(), 300, new Item.Settings().group(chestGroup)));
        this.itemRegister.register(trappedChestName, () ->
                new FuelBlockItem(trappedChest.get(), 300, new Item.Settings().group(trappedChestGroup)));
        ChestManager.putChestInfo(modId, name, false);
        ChestManager.putChestInfo(modId, name, true);
        return Pair.of(chest, trappedChest);
    }

    /**
     * Creates and registers a {@link BlueprintChestBlock} and a {@link BlueprintTrappedChestBlock} with their {@link FuelBlockItem}s.
     *
     * @param name   The name for the chest blocks.
     * @param color  The {@link MapColor} for the chest blocks.
     * @param modIds The mod ids of the mods this block is compatible for.
     * @return A {@link Pair} containing {@link RegistryObject}s of the {@link BlueprintChestBlock} and the {@link BlueprintTrappedChestBlock}.
     */
    public Pair<RegistryObject<BlueprintChestBlock>, RegistryObject<BlueprintTrappedChestBlock>> createCompatChestBlocks(String name, MapColor color, String... modIds) {
        boolean isInGroup = areModsLoaded(modIds);
        ItemGroup chestGroup = isInGroup ? ItemGroup.DECORATIONS : null;
        ItemGroup trappedChestGroup = isInGroup ? ItemGroup.REDSTONE : null;
        String modId = this.parent.getModId();
        String chestName = name + "_chest";
        String trappedChestName = name + "_trapped_chest";
        RegistryObject<BlueprintChestBlock> chest = this.deferredRegister.register(chestName, () ->
                new BlueprintChestBlock(modId + ":" + name, AbstractBlock.Settings.of(Material.WOOD, color).strength(2.5F).sounds(BlockSoundGroup.WOOD)));
        RegistryObject<BlueprintTrappedChestBlock> trappedChest = this.deferredRegister.register(trappedChestName, () ->
                new BlueprintTrappedChestBlock(modId + ":" + name + "_trapped", AbstractBlock.Settings.of(Material.WOOD, color).strength(2.5F).sounds(BlockSoundGroup.WOOD)));
        this.itemRegister.register(chestName, () ->
                new FuelBlockItem(chest.get(), 300, new Item.Settings().group(chestGroup)));
        this.itemRegister.register(trappedChestName, () ->
                new FuelBlockItem(trappedChest.get(), 300, new Item.Settings().group(trappedChestGroup)));
        ChestManager.putChestInfo(modId, name, false);
        ChestManager.putChestInfo(modId, name, true);
        return Pair.of(chest, trappedChest);
    }
}
