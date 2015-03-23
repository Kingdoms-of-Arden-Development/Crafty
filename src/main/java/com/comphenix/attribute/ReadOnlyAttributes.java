package com.comphenix.attribute;

import com.comphenix.attribute.NbtFactory.NbtCompound;
import org.bukkit.inventory.ItemStack;

/**
 * Hacky way of solving creation of empty nbt lists (making items non-stackable)
 */
public class ReadOnlyAttributes extends Attributes {

    public ReadOnlyAttributes(ItemStack stack) {
        super(stack);
    }

    @Override
    protected void loadAttributes(boolean createIfMissing) {
        if (this.attributes == null) {
            NbtCompound nbt = NbtFactory.fromItemTagClean(this.stack);
            this.attributes = nbt.getList("AttributeModifiers", createIfMissing);
        }
    }
}
