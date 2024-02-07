package net.george.blueprint.core.util.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.core.api.registry.DeferredRegister;

/**
 * An abstract implementation class of {@link ISubRegistryHelper}.
 * This contains a {@link RegistryHelper} parent and a {@link DeferredRegister} to register objects.
 * <p> It is recommended you use this for making a new {@link ISubRegistryHelper}. </p>
 *
 * @param <T> The type of registry entry to register objects for.
 * @author SmellyModder (Luke Tonon)
 * @see ISubRegistryHelper
 */
@SuppressWarnings("unused")
public abstract class AbstractSubRegistryHelper<T> implements ISubRegistryHelper<T> {
    protected final RegistryHelper parent;
    protected final DeferredRegister<T> deferredRegister;

    public AbstractSubRegistryHelper(RegistryHelper parent, DeferredRegister<T> deferredRegister) {
        this.parent = parent;
        this.deferredRegister = deferredRegister;
    }

    /**
     * @return The parent {@link RegistryHelper} this is a child of.
     */
    @Override
    public RegistryHelper getParent() {
        return this.parent;
    }

    /**
     * @return The {@link DeferredRegister} belonging to this {@link AbstractSubRegistryHelper}.
     */
    @Override
    public DeferredRegister<T> getDeferredRegister() {
        return this.deferredRegister;
    }

    /**
     * Registers this {@link AbstractSubRegistryHelper}.
     */
    @Override
    public void register() {
        this.getDeferredRegister().register();
    }

    /**
     * Determines whether a group of mods is loaded.
     *
     * @param modIds The mod ids of the mods to check.
     * @return A boolean representing whether all the mods passed in are loaded.
     */
    public static boolean areModsLoaded(String... modIds) {
        for (String mod : modIds) {
            if (!FabricLoader.getInstance().isModLoaded(mod)) {
                return false;
            }
        }
        return true;
    }
}
