package net.george.blueprint.core.mixin.extension;

import net.george.blueprint.common.world.modification.ModdedBiomeSource;
import net.george.blueprint.common.world.modification.ModdedSurfaceSystem;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(SurfaceBuilder.class)
public final class SurfaceBuilderMixin implements ModdedSurfaceSystem {
    @Unique
    @Nullable
    private ModdedBiomeSource moddedBiomeSource;

    public void setModdedBiomeSource(@Nullable ModdedBiomeSource moddedBiomeSource) {
        this.moddedBiomeSource = moddedBiomeSource;
    }

    @Override
    @Nullable
    public ModdedBiomeSource getModdedBiomeSource() {
        return this.moddedBiomeSource;
    }
}
