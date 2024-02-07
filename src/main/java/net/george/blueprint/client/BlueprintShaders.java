package net.george.blueprint.client;

import com.mojang.datafixers.util.Pair;
import net.george.blueprint.core.events.RegisteredShadersCallback;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * The class for all of Blueprint's shaders.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class BlueprintShaders {
    @Nullable
    private static Shader rendertypeEntityUnshadedCutout;
    @Nullable
    private static Shader rendertypeEntityUnshadedTranslucent;

    public static void registerShaders() {
        RegisteredShadersCallback.EVENT.register((manager, shaders) -> {
            try {
                shaders.add(Pair.of(new Shader(manager, "rendertype_entity_unshaded_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), shaderInstance -> rendertypeEntityUnshadedCutout = shaderInstance));
                shaders.add(Pair.of(new Shader(manager, "rendertype_entity_unshaded_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), shaderInstance -> rendertypeEntityUnshadedTranslucent = shaderInstance));
            } catch (IOException exception) {
                throw new RuntimeException("Could not reload Blueprint's shaders!", exception);
            }
        });
    }

    /**
     * Gets the {@link Shader} used for unshaded cutout entities.
     *
     * @return The {@link Shader} used for unshaded cutout entities.
     * @see BlueprintRenderTypes#getUnshadedCutoutEntity(net.minecraft.util.Identifier, boolean)
     */
    @Nullable
    public static Shader getRendertypeEntityUnshadedCutout() {
        return rendertypeEntityUnshadedCutout;
    }

    /**
     * Gets the {@link Shader} used for unshaded translucent entities.
     *
     * @return The {@link Shader} used for unshaded translucent entities.
     * @see BlueprintRenderTypes#getUnshadedTranslucentEntity(net.minecraft.util.Identifier, boolean)
     */
    @Nullable
    public static Shader getRendertypeEntityUnshadedTranslucent() {
        return rendertypeEntityUnshadedTranslucent;
    }
}
