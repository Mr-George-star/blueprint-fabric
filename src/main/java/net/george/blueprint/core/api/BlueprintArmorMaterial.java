package net.george.blueprint.core.api;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

import java.util.function.Supplier;

/**
 * An {@link ArmorMaterial} implementation made for simple creation of {@link ArmorMaterial} instances.
 */
@SuppressWarnings({"deprecation", "unused"})
public class BlueprintArmorMaterial implements ArmorMaterial {
    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final Identifier name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantability;
    private final Supplier<SoundEvent> sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Lazy<Ingredient> repairIngredient;

    public BlueprintArmorMaterial(Identifier name, int durabilityMultiplier, int[] slotProtections, int enchantability, Supplier<SoundEvent> sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantability = enchantability;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new Lazy<>(repairIngredient);
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return HEALTH_PER_SLOT[slot.getEntitySlotId()] * this.durabilityMultiplier;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return this.slotProtections[slot.getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.sound.get();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return this.name.toString();
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
