package net.george.blueprint.core.mixin.extension;

import com.google.common.base.Suppliers;
import net.george.blueprint.common.world.modification.ModdedBiomeSource;
import net.george.blueprint.common.world.modification.ModdedSurfaceSystem;
import net.george.blueprint.common.world.modification.ModdednessSliceGetter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(MaterialRules.MaterialRuleContext.class)
public class MaterialRuleContextMixin implements ModdednessSliceGetter {
    @Unique
    @Nullable
    private Supplier<Identifier> moddedBiomeSlice;
    @Unique
    @Nullable
    private ModdedBiomeSource moddedBiomeSource;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void initModdedBiomeSource(SurfaceBuilder surfaceBuilder, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, Function<BlockPos, RegistryEntry<Biome>> posToBiome, Registry<Biome> biomeRegistry, HeightContext heightContext, CallbackInfo ci) {
        this.moddedBiomeSource = ((ModdedSurfaceSystem)surfaceBuilder).getModdedBiomeSource();
    }

    @Inject(at = @At("RETURN"), method = "initVerticalContext")
    private void updateModdedBiomeSlice(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int x, int y, int z, CallbackInfo ci) {
        ModdedBiomeSource moddedBiomeSource = this.moddedBiomeSource;
        if (moddedBiomeSource != null) {
            this.moddedBiomeSlice = Suppliers.memoize(() -> moddedBiomeSource.getSliceWithVanillaZoom(x, y, z).name());
        }

    }

    @Override
    public boolean cannotGetSlices() {
        return this.moddedBiomeSource == null;
    }

    @Override
    public Identifier getSliceName() {
        return this.moddedBiomeSlice.get();
    }
}
