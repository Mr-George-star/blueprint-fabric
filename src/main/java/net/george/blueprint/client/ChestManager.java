package net.george.blueprint.client;

import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.blueprint.core.events.TextureStitchedCallback;
import net.george.blueprint.core.util.registry.BlockSubRegistryHelper;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manager class for texture information for Blueprint Chests.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public final class ChestManager {
    private static final Map<String, ChestInfo> CHEST_INFO_MAP = new HashMap<>();

    /**
     * Puts a created {@link ChestInfo} onto the {@link #CHEST_INFO_MAP} for a given ID and type.
     * <p>Called in chest related methods in {@link BlockSubRegistryHelper}</p>
     *
     * @param modId   Mod ID for the chest.
     * @param type    Type for the chest. (e.g. "oak")
     * @param trapped If the chest is trapped.
     */
    public static synchronized void putChestInfo(String modId, String type, boolean trapped) {
        CHEST_INFO_MAP.put(modId + ":" + type + (trapped ? "_trapped" : ""), new ChestInfo(modId, type, trapped));
    }

    /**
     * Gets the {@link ChestInfo} for a given chest type.
     *
     * @param chestType A string for the {@link ChestInfo} to lookup.
     * @return The {@link ChestInfo} for the given chest type, or null if there is no {@link ChestInfo} for the given type.
     */
    @Nullable
    public static ChestInfo getInfoForChest(String chestType) {
        return CHEST_INFO_MAP.get(chestType);
    }

    @Environment(EnvType.CLIENT)
    public static void registerEvents() {
        TextureStitchedCallback.EVENT.register((texture, set) -> {
            Set<Identifier> result = Sets.newHashSet();
            if (texture.getId().equals(TexturedRenderLayers.CHEST_ATLAS_TEXTURE)) {
                for (ChestInfo info : CHEST_INFO_MAP.values()) {
                    info.setup(result);
                }
            }
            return result;
        });
    }

    public static class ChestInfo {
        private final Identifier single, left, right;
        @Environment(EnvType.CLIENT)
        private SpriteIdentifier singleMaterial, leftMaterial, rightMaterial;

        public ChestInfo(String modId, String type, boolean trapped) {
            String chest = trapped ? "trapped" : "normal";
            this.single = new Identifier(modId, "entity/chest/" + type + "/" + chest);
            this.left = new Identifier(modId, "entity/chest/" + type + "/" + chest + "_left");
            this.right = new Identifier(modId, "entity/chest/" + type + "/" + chest + "_right");
        }

        /**
         * Adds the internal textures to the stitch event and initializes the {@link SpriteIdentifier}s.
         *
         * @param set A set to set up this info from.
         */
        @Environment(EnvType.CLIENT)
        private void setup(Set<Identifier> set) {
            set.add(this.single);
            set.add(this.left);
            set.add(this.right);
            this.singleMaterial = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, this.single);
            this.leftMaterial = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, this.left);
            this.rightMaterial = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, this.right);
        }

        /**
         * Gets this info's default/single {@link SpriteIdentifier}.
         *
         * @return This info's default/single {@link SpriteIdentifier}.
         */
        @Environment(EnvType.CLIENT)
        public SpriteIdentifier getSingleMaterial() {
            return this.singleMaterial;
        }

        /**
         * Gets this info's left {@link SpriteIdentifier}.
         *
         * @return This info's left {@link SpriteIdentifier}.
         */
        @Environment(EnvType.CLIENT)
        public SpriteIdentifier getLeftMaterial() {
            return this.leftMaterial;
        }

        /**
         * Gets this info's right {@link SpriteIdentifier}.
         *
         * @return This info's right {@link SpriteIdentifier}.
         */
        @Environment(EnvType.CLIENT)
        public SpriteIdentifier getRightMaterial() {
            return this.rightMaterial;
        }
    }
}
