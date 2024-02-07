package net.george.blueprint.core.util.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.george.blueprint.common.network.entity.SpawnEntityS2CPacket;
import net.george.blueprint.core.api.registry.DeferredRegister;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.minecraft.entity.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.function.BiFunction;

/**
 * A basic {@link AbstractSubRegistryHelper} for entities.
 * <p>This contains some useful registering methods for entities.</p>
 *
 * @author SmellyModder (Luke Tonon)
 * @see AbstractSubRegistryHelper
 */
@SuppressWarnings("unused")
public class EntitySubRegistryHelper extends AbstractSubRegistryHelper<EntityType<?>> {
    public EntitySubRegistryHelper(RegistryHelper parent, DeferredRegister<EntityType<?>> deferredRegister) {
        super(parent, deferredRegister);
    }

    public EntitySubRegistryHelper(RegistryHelper parent) {
        super(parent, DeferredRegister.of(Registry.ENTITY_TYPE, parent.getModId()));
    }

    /**
     * Creates and registers an {@link EntityType} with the type of {@link LivingEntity}.
     *
     * @param name                 The entity's name.
     * @param factory              The entity's factory.
     * @param entityClassification The entity's classification.
     * @param width                The width of the entity's bounding box.
     * @param height               The height of the entity's bounding box.
     * @return A {@link RegistryObject} containing the created {@link EntityType}.
     */
    public <E extends LivingEntity> RegistryObject<EntityType<E>> createLivingEntity(String name, EntityType.EntityFactory<E> factory, SpawnGroup entityClassification, float width, float height) {
        return this.deferredRegister.register(name, () -> createLivingEntity(factory, entityClassification, name, width, height));
    }

    /**
     * Creates and registers an {@link EntityType} with the type of {@link Entity}.
     *
     * @param name                 The entity's name.
     * @param factory              The entity's factory.
     * @param clientFactory        The entity's client factory.
     * @param entityClassification The entity's classification.
     * @param width                The width of the entity's bounding box.
     * @param height               The height of the entity's bounding box.
     * @return A {@link RegistryObject} containing the created {@link EntityType}.
     */
    public <E extends Entity> RegistryObject<EntityType<E>> createEntity(String name, EntityType.EntityFactory<E> factory, BiFunction<SpawnEntityS2CPacket, World, E> clientFactory, SpawnGroup entityClassification, float width, float height) {
        return this.deferredRegister.register(name, () -> createEntity(factory, clientFactory, entityClassification, name, width, height));
    }

    /**
     * Creates an {@link EntityType} with the type of {@link LivingEntity}.
     *
     * @param name                 The entity's name.
     * @param factory              The entity's factory.
     * @param entityClassification The entity's classification.
     * @param width                The width of the entity's bounding box.
     * @param height               The height of the entity's bounding box.
     * @return The created {@link EntityType}.
     */
    public <E extends LivingEntity> EntityType<E> createLivingEntity(EntityType.EntityFactory<E> factory, SpawnGroup entityClassification, String name, float width, float height) {
        Identifier id = this.parent.prefix(name);
        return FabricEntityTypeBuilder.create(entityClassification, factory).dimensions(EntityDimensions.fixed(width, height))
                .trackRangeBlocks(64)
                .forceTrackedVelocityUpdates(true)
                .trackedUpdateRate(3)
                .build();
    }

    /**
     * Creates an {@link EntityType} with the type of {@link Entity}.
     *
     * @param name                 The entity's name.
     * @param factory              The entity's factory.
     * @param clientFactory        The entity's client factory.
     * @param entityClassification The entity's classification.
     * @param width                The width of the entity's bounding box.
     * @param height               The height of the entity's bounding box.
     * @return The created {@link EntityType}.
     */
    public <E extends Entity> EntityType<E> createEntity(EntityType.EntityFactory<E> factory, BiFunction<SpawnEntityS2CPacket, World, E> clientFactory, SpawnGroup entityClassification, String name, float width, float height) {
        Identifier id = this.parent.prefix(name);
        return FabricEntityTypeBuilder.create(entityClassification, factory).dimensions(EntityDimensions.fixed(width, height))
                .trackRangeBlocks(64)
                .forceTrackedVelocityUpdates(true)
                .trackedUpdateRate(3)
                .build();
    }

}
