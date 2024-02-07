package net.george.blueprint.core.util.registry;

import net.george.blueprint.core.api.registry.DeferredRegister;
import net.george.blueprint.core.api.registry.RegistryObject;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

/**
 * A basic {@link AbstractSubRegistryHelper} for sounds. This contains some useful registering methods for sounds.
 *
 * @author SmellyModder (Luke Tonon)
 * @see AbstractSubRegistryHelper
 */
@SuppressWarnings("unused")
public class SoundSubRegistryHelper extends AbstractSubRegistryHelper<SoundEvent> {
    public SoundSubRegistryHelper(RegistryHelper parent, DeferredRegister<SoundEvent> deferredRegister) {
        super(parent, deferredRegister);
    }

    public SoundSubRegistryHelper(RegistryHelper parent) {
        super(parent, DeferredRegister.of(Registry.SOUND_EVENT, parent.getModId()));
    }

    /**
     * Creates and registers a {@link SoundEvent}.
     *
     * @param name The sound's name.
     * @return A {@link RegistryObject} containing the created {@link SoundEvent}.
     */
    public RegistryObject<SoundEvent> createSoundEvent(String name) {
        return this.deferredRegister.register(name, () -> new SoundEvent(this.parent.prefix(name)));
    }
}
