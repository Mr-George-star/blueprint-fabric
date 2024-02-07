package net.george.blueprint.core.util.extension;

import net.minecraft.util.Identifier;

public interface IdentifierExtension {
    default int compareNamespace(Identifier id) {
        throw new UnsupportedOperationException("This method should be overwritten by mixin!");
    }
}
