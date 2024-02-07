package net.george.blueprint.core.mixin.accessor;

import net.minecraft.util.SignType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SignType.class)
public interface SignTypeAccessor {
    @Invoker("<init>")
    static SignType create(String name) {
        throw new UnsupportedOperationException();
    }
}
