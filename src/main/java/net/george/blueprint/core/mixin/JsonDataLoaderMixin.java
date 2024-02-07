package net.george.blueprint.core.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.george.blueprint.core.events.SimpleJsonResourceListenerPreparedEvent;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(JsonDataLoader.class)
public class JsonDataLoaderMixin {
    @Shadow @Final
    private Gson gson;
    @Shadow @Final
    private String dataType;

    @Inject(method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/Map;", at = @At("RETURN"))
    private void onPrepared(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<Map<Identifier, JsonElement>> cir) {
        SimpleJsonResourceListenerPreparedEvent.EVENT.invoker().interact(this.gson, this.dataType, cir.getReturnValue());
    }
}
