package net.george.blueprint.core.mixin;

import net.george.blueprint.core.util.extension.IdentifierExtension;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public class IdentifierMixin implements IdentifierExtension {
    @Shadow @Final
    protected String namespace;
    @Shadow @Final
    protected String path;

    @Override
    public int compareNamespace(Identifier id) {
        int result = this.namespace.compareTo(id.getNamespace());
        return result != 0 ? result : this.path.compareTo(id.getPath());
    }
}
