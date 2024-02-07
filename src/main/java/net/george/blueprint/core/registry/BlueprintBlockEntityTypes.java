package net.george.blueprint.core.registry;

import net.george.blueprint.common.block.BlueprintBeehiveBlock;
import net.george.blueprint.common.block.chest.BlueprintChestBlock;
import net.george.blueprint.common.block.chest.BlueprintTrappedChestBlock;
import net.george.blueprint.common.block.entity.BlueprintBeehiveBlockEntity;
import net.george.blueprint.common.block.entity.BlueprintChestBlockEntity;
import net.george.blueprint.common.block.entity.BlueprintSignBlockEntity;
import net.george.blueprint.common.block.entity.BlueprintTrappedChestBlockEntity;
import net.george.blueprint.common.block.sign.IBlueprintSign;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.george.blueprint.core.util.registry.BlockEntitySubRegistryHelper;
import net.minecraft.block.entity.BlockEntityType;

public class BlueprintBlockEntityTypes {
    private static final BlockEntitySubRegistryHelper HELPER = Blueprint.REGISTRY_HELPER.getBlockEntitySubHelper();

    public static final RegistryObject<BlockEntityType<BlueprintSignBlockEntity>> SIGN = HELPER
            .createBlockEntity("sign", BlueprintSignBlockEntity::new, () -> BlockEntitySubRegistryHelper.collectBlocks(IBlueprintSign.class));
    public static final RegistryObject<BlockEntityType<BlueprintBeehiveBlockEntity>> BEEHIVE = HELPER
            .createBlockEntity("beehive", BlueprintBeehiveBlockEntity::new, BlueprintBeehiveBlock.class);
    public static final RegistryObject<BlockEntityType<BlueprintChestBlockEntity>> CHEST = HELPER
            .createBlockEntity("chest", BlueprintChestBlockEntity::new, BlueprintChestBlock.class);
    public static final RegistryObject<BlockEntityType<BlueprintTrappedChestBlockEntity>> TRAPPED_CHEST = HELPER
            .createBlockEntity("trapped_chest", BlueprintTrappedChestBlockEntity::new, BlueprintTrappedChestBlock.class);

    public static void register() {
        HELPER.register();
        Blueprint.LOGGER.debug("Registering Block Entity Type for " + Blueprint.MOD_ID + "!");
    }
}
