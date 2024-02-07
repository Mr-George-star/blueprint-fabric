package net.george.blueprint.core.api;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Lazy;

import java.util.function.Supplier;

/**
 * A {@link ToolMaterial} implementation made for simple creation of {@link ToolMaterial} instances.
 */
@SuppressWarnings({"deprecation", "unused"})
public class BlueprintItemTier implements ToolMaterial {
    private final int level;
    private final int durability;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final Lazy<Ingredient> repairIngredient;

    public BlueprintItemTier(int miningLevel, int durability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient) {
        this.level = miningLevel;
        this.durability = durability;
        this.speed = miningSpeed;
        this.damage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = new Lazy<>(repairIngredient);
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.speed;
    }

    @Override
    public float getAttackDamage() {
        return this.damage;
    }

    @Override
    public int getMiningLevel() {
        return this.level;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
