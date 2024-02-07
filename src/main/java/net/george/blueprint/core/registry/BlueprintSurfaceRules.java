package net.george.blueprint.core.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.george.blueprint.common.world.modification.ModdednessSliceGetter;
import net.george.blueprint.core.Blueprint;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

/**
 * The class for Blueprint's surface rule types.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class BlueprintSurfaceRules extends MaterialRules {
    /**
     * Registers Blueprint's surface rule types.
     * <p><b>This is for internal use only!</b></p>
     */
    public static void register() {
        BuiltinRegistries.add(Registry.MATERIAL_CONDITION, new Identifier(Blueprint.MOD_ID, "moddedness_slice"), ModdednessSliceConditionSource.CODEC);
    }

    /**
     * A {@link MaterialCondition} implementation that checks for a named moddedness slice.
     *
     * @author SmellyModder (Luke Tonon)
     */
    public record ModdednessSliceConditionSource(Identifier sliceName) implements MaterialCondition {
        public static final Codec<ModdednessSliceConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("slice_name").forGetter(condition -> condition.sliceName)
        ).apply(instance, ModdednessSliceConditionSource::new));

        @Override
        public Codec<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRuleContext materialRuleContext) {
            ModdednessSliceGetter moddednessSliceGetter = ModdednessSliceGetter.class.cast(materialRuleContext);
            if (moddednessSliceGetter.cannotGetSlices()) {
                return () -> false;
            }

            class ModdednessSliceCondition extends MaterialRules.FullLazyAbstractPredicate {
                ModdednessSliceCondition() {
                    super(materialRuleContext);
                }

                @Override
                protected boolean test() {
                    return moddednessSliceGetter.getSliceName().equals(ModdednessSliceConditionSource.this.sliceName);
                }
            }

            return new ModdednessSliceCondition();
        }
    }
}
