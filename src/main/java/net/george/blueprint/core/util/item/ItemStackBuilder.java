package net.george.blueprint.core.util.item;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

/**
 * A simple utility class for building {@link ItemStack}s.
 *
 * @author SmellyModder (Luke Tonon)
 */
@SuppressWarnings("unused")
public class ItemStackBuilder {
    private final ItemStack stack;
    private final NbtCompound nbt;

    public ItemStackBuilder(ItemStack stack) {
        this.stack = stack;
        this.nbt = stack.getOrCreateNbt();
    }

    public ItemStackBuilder(ItemConvertible item) {
        this(new ItemStack(item));
    }

    /**
     * Sets the stack's count.
     *
     * @return This builder.
     * @see ItemStack#setCount(int).
     */
    public ItemStackBuilder setCount(int count) {
        this.stack.setCount(count);
        return this;
    }

    /**
     * Grows the stack by an amount.
     *
     * @param amount Amount to grow the stack by.
     * @return This builder.
     * @see ItemStack#increment(int).
     */
    public ItemStackBuilder grow(int amount) {
        this.stack.increment(amount);
        return this;
    }

    /**
     * Shrinks the stack by an amount.
     *
     * @param amount Amount to shrink the stack by.
     * @return This builder.
     * @see ItemStack#decrement(int).
     */
    public ItemStackBuilder shrink(int amount) {
        this.stack.decrement(amount);
        return this;
    }

    /**
     * Sets the stack unbreakable.
     *
     * @return This builder.
     */
    public ItemStackBuilder setUnbreakable() {
        this.nbt.putBoolean("Unbreakable", true);
        return this;
    }

    /**
     * Adds an enchantment with a level to the stack.
     *
     * @param enchantment The {@link Enchantment} to add.
     * @param level       The level of the {@link Enchantment} to add.
     * @return This builder.
     */
    public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
        this.stack.addEnchantment(enchantment, level);
        return this;
    }

    /**
     * Sets the name of the stack.
     *
     * @param text The name to set.
     * @return This builder.
     * @see ItemStack#setCustomName(Text) .
     */
    public ItemStackBuilder setName(@Nullable Text text) {
        this.stack.setCustomName(text);
        return this;
    }

    /**
     * Adds lore to the stack.
     *
     * @param text The lore text to add.
     * @return This builder.
     */
    public ItemStackBuilder addLore(Text text) {
        NbtCompound display = this.stack.getOrCreateSubNbt("display");
        NbtList loreListTag;
        if (display.contains("Lore", 9)) {
            loreListTag = display.getList("Lore", 8);
        } else {
            loreListTag = new NbtList();
            display.put("Lore", loreListTag);
        }
        loreListTag.add(NbtString.of(Text.Serializer.toJson(text)));
        return this;
    }

    /**
     * Adds an {@link EntityAttributeModifier} for an {@link EntityAttribute} for an {@link EquipmentSlot} on the stack.
     *
     * @param attribute The attribute to apply the {@link EntityAttributeModifier} for.
     * @param modifier  The {@link EntityAttributeModifier} to apply to the {@link EntityAttribute}.
     * @param slot      The slot for when the {@link EntityAttributeModifier} should be applied.
     * @return This builder.
     * @see ItemStack#addAttributeModifier(EntityAttribute, EntityAttributeModifier, EquipmentSlot) .
     */
    public ItemStackBuilder addAttributeModifier(EntityAttribute attribute, EntityAttributeModifier modifier, @Nullable EquipmentSlot slot) {
        this.stack.addAttributeModifier(attribute, modifier, slot);
        return this;
    }

    /**
     * Adds an {@link EntityAttributeModifier} for an {@link EntityAttribute} for a multiple {@link EquipmentSlot}s on the stack.
     *
     * @param attribute The attribute to apply the {@link EntityAttributeModifier} for.
     * @param modifier  The {@link EntityAttributeModifier} to apply to the {@link EntityAttribute}.
     * @param slots     The slots for when the {@link EntityAttributeModifier} should be applied.
     * @return This builder.
     * @see ItemStack#addAttributeModifier(EntityAttribute, EntityAttributeModifier, EquipmentSlot) .
     * @see #addAttributeModifier(EntityAttribute, EntityAttributeModifier, EquipmentSlot) .
     */
    public ItemStackBuilder addAttributeModifier(EntityAttribute attribute, EntityAttributeModifier modifier, EquipmentSlot... slots) {
        for (EquipmentSlot slot : slots) {
            this.stack.addAttributeModifier(attribute, modifier, slot);
        }
        return this;
    }

    /**
     * Adds a predicate string tag for a predicate key.
     * The two types of predicate keys are "CanDestroy" and "CanPlace".
     *
     * @param key       The predicate key.
     * @param predicate The predicate string, this should be a string id.
     * @return This builder.
     */
    public ItemStackBuilder addPredicate(String key, String predicate) {
        NbtList predicateList;
        if (this.nbt.contains(key, 9)) {
            predicateList = this.nbt.getList(key, 8);
        } else {
            predicateList = new NbtList();
            this.nbt.put(key, predicateList);
        }
        predicateList.add(NbtString.of(predicate));
        return this;
    }

    /**
     * Adds a can destroy predicate for a specific block.
     *
     * @param block The block to mark as able to be destroyed.
     * @return This builder.
     */
    public ItemStackBuilder addCanDestroy(Block block) {
        return this.addPredicate("CanDestroy", Registry.BLOCK.getKey(block).get().toString());
    }

    /**
     * Adds a can place on predicate for a specific block.
     *
     * @param block The block to mark as able to be placed on.
     * @return This builder.
     */
    public ItemStackBuilder addCanPlaceOn(Block block) {
        return this.addPredicate("CanPlaceOn", Registry.BLOCK.getKey(block).get().toString());
    }

    /**
     * @return The built stack.
     */
    public ItemStack build() {
        return this.stack.copy();
    }
}
