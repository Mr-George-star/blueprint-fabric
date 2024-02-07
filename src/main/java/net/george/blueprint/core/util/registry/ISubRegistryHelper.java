package net.george.blueprint.core.util.registry;

import net.george.blueprint.core.api.registry.DeferredRegister;

/**
 * An interface for 'sub' registry helpers used in {@link RegistryHelper}.
 *
 * @param <T> The type of registry entry this is for.
 * @author SmellyModder (Luke Tonon)
 */
public interface ISubRegistryHelper<T> {
    /**
     * @return The {@link RegistryHelper} this is a child of.
     */
    RegistryHelper getParent();

    /**
     * @return The {@link DeferredRegister} for registering.
     */
    DeferredRegister<T> getDeferredRegister();

    /**
     * Should ideally register {@link #getDeferredRegister()}.
     */
    void register();
}
