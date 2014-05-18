package net.kingdomsofarden.crafty.internals;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class CacheKey {
    
    private final ItemStack item;
    private final UUID itemId;
    
    public CacheKey(ItemStack item, UUID id) {
        this.item = item;
        this.itemId = id;
    }
    
    @Override
    public int hashCode() {
        return itemId.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if (obj instanceof CacheKey) {
            if(itemId.equals(((CacheKey)obj).itemId)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public ItemStack getItem() {
        return item;
    }
    
    public UUID getItemUuid() {
        return this.itemId;
    }
    
}
