package net.george.blueprint.core.other;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum BlueprintCoordinationMods implements StringIdentifiable {
    WOODWORKS,
    QUARK;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
