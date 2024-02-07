package net.george.blueprint.client;

import net.george.blueprint.core.Blueprint;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * A class containing some methods for creating useful {@link RenderLayer} instances.
 *
 * @author SmellyModder(Luke Tonon)
 */
public class BlueprintRenderTypes extends RenderLayer {
    public static final Shader RENDERTYPE_ENTITY_UNSHADED_CUTOUT_SHADER = new Shader(BlueprintShaders::getRendertypeEntityUnshadedCutout);
    public static final Shader RENDERTYPE_ENTITY_UNSHADED_TRANSLUCENT_SHADER = new Shader(BlueprintShaders::getRendertypeEntityUnshadedTranslucent);

    public BlueprintRenderTypes(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    /**
     * Creates a new unshaded {@link RenderLayer} for cutout entities for a given texture.
     *
     * @param texture A {@link Identifier} to use for the texture.
     * @param outline If the {@link RenderLayer} should affect the outline effect.
     * @return A new unshaded {@link RenderLayer} for cutout entities for a given texture.
     */
    public static RenderLayer getUnshadedCutoutEntity(Identifier texture, boolean outline) {
        MultiPhaseParameters state = MultiPhaseParameters.builder()
                .texture(new Texture(texture, false, false)).shader(RENDERTYPE_ENTITY_UNSHADED_CUTOUT_SHADER)
                .transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
                .build(outline);
        return of(Blueprint.MOD_ID + ":entity_unshaded_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, state);
    }

    /**
     * Creates a new unshaded and translucent {@link RenderLayer} for entities for a given texture.
     *
     * @param texture A {@link Identifier} to use for the texture.
     * @param outline If the {@link RenderLayer} should affect the outline effect.
     * @return A new unshaded and translucent {@link RenderLayer} for entities for a given texture.
     */
    public static RenderLayer getUnshadedTranslucentEntity(Identifier texture, boolean outline) {
        MultiPhaseParameters state = MultiPhaseParameters.builder()
                .texture(new Texture(texture, false, false)).shader(RENDERTYPE_ENTITY_UNSHADED_TRANSLUCENT_SHADER)
                .transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
                .build(outline);
        return of(Blueprint.MOD_ID + ":entity_unshaded_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, state);
    }
}
