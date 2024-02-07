package net.george.blueprint.core.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.SignType;

import java.util.HashSet;
import java.util.Set;

/**
 * Manager class for Blueprint's custom sign system.
 */
@SuppressWarnings("unused")
public final class SignManager {
    private static final Set<SignType> WOOD_TYPES = new HashSet<>();

    @Environment(EnvType.CLIENT)
    public static void setupAtlas() {
        for (SignType type : WOOD_TYPES)
            TexturedRenderLayers.WOOD_TYPE_TEXTURES.put(type, TexturedRenderLayers.createSignTextureId(type));
    }

    /**
     * Registers a {@link SignType} to the {@link #WOOD_TYPES} map.
     * <p>This method is safe to call during parallel mod loading.</p>
     *
     * @param type A {@link SignType} to register.
     * @return The registered {@link SignType}.
     */
    public static synchronized SignType registerWoodType(SignType type) {
        WOOD_TYPES.add(type);
        return SignType.register(type);
    }
}
