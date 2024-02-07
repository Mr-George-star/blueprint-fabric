package net.george.blueprint.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.george.blueprint.core.events.TextureStitchedCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
    @Inject(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V"))
    public void stitch(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler,
                       int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir, @Local Set<Identifier> set) {
        Set<Identifier> invoked = TextureStitchedCallback.EVENT.invoker().interact((SpriteAtlasTexture)(Object)this, set);
        set.addAll(invoked);
    }
}
