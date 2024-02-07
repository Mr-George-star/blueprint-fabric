package net.george.blueprint.core.util.registry;

import com.google.common.collect.Sets;
import net.george.blueprint.core.api.registry.DeferredRegister;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

import static net.minecraft.block.entity.BlockEntityType.BlockEntityFactory;

/**
 * A basic {@link AbstractSubRegistryHelper} for block entities.
 * <p>This contains a few registering methods for block entities.</p>
 *
 * @author SmellyModder (Luke Tonon)
 * @see AbstractSubRegistryHelper
 */
@SuppressWarnings("unused")
public class BlockEntitySubRegistryHelper extends AbstractSubRegistryHelper<BlockEntityType<?>> {
    public BlockEntitySubRegistryHelper(RegistryHelper parent, DeferredRegister<BlockEntityType<?>> deferredRegister) {
        super(parent, deferredRegister);
    }

    public BlockEntitySubRegistryHelper(RegistryHelper parent) {
        super(parent, DeferredRegister.of(Registry.BLOCK_ENTITY_TYPE, parent.getModId()));
    }

    /**
     * Collects all registered {@link Block}s that are an instance of a {@link Block} class.
     *
     * @param blockClass The instance of class to filter
     * @return A filtered array of registered {@link Block}s that are an instance of a {@link Block} class
     */
    public static Block[] collectBlocks(Class<?> blockClass) {
        return Registry.BLOCK.stream().filter(blockClass::isInstance).toArray(Block[]::new);
    }

    /**
     * Creates and registers a {@link BlockEntityType}.
     *
     * @param name        The name for the {@link BlockEntity}.
     * @param blockEntity The {@link BlockEntity}.
     * @param validBlocks The valid blocks for this {@link BlockEntityType}.
     * @return A {@link RegistryObject} containing the customized {@link BlockEntityType}.
     */
    public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> createBlockEntity(String name, BlockEntityFactory<? extends T> blockEntity, Supplier<Block[]> validBlocks) {
        return this.deferredRegister.register(name, () -> new BlockEntityType<>(blockEntity, Sets.newHashSet(validBlocks.get()), null));
    }

    /**
     * Creates and registers a {@link BlockEntityType} with valid blocks that are an instance of a {@link Block} class.
     * <p>Useful for dynamic valid blocks on block entities.</p>
     *
     * @param name        The name for the {@link BlockEntity}.
     * @param blockEntity The {@link BlockEntity}.
     * @param blockClass  The block class to filter registered blocks that are an instance of it.
     * @return A {@link RegistryObject} containing the customized {@link BlockEntityType}.
     */
    public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> createBlockEntity(String name, BlockEntityFactory<? extends T> blockEntity, Class<? extends Block> blockClass) {
        return this.deferredRegister.register(name, () -> new BlockEntityType<>(blockEntity, Sets.newHashSet(collectBlocks(blockClass)), null));
    }
}
