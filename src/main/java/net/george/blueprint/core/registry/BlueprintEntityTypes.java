package net.george.blueprint.core.registry;

import net.george.blueprint.common.entity.BlueprintBoat;
import net.george.blueprint.core.Blueprint;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.george.blueprint.core.util.registry.EntitySubRegistryHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

/**
 * Registry class for the built-in {@link EntityType}s.
 */
public final class BlueprintEntityTypes {
    private static final EntitySubRegistryHelper HELPER = Blueprint.REGISTRY_HELPER.getEntitySubHelper();

    public static final RegistryObject<EntityType<BlueprintBoat>> BOAT = HELPER.createEntity("boat", BlueprintBoat::new, BlueprintBoat::new, SpawnGroup.MISC, 1.375F, 0.5625F);

    public static void register() {
        Blueprint.LOGGER.debug("Registering Entity Types for " + Blueprint.MOD_ID + "!");
        HELPER.register();
    }
}
