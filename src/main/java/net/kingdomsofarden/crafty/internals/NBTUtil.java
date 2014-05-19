package net.kingdomsofarden.crafty.internals;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.comphenix.attribute.AttributeStorage;

public class NBTUtil {
    
    private static final UUID ITEM_TRACKER;
    
    static {
        ITEM_TRACKER = UUID.fromString("198d8160-c487-11e3-9c1a-0800200c9a66");
    }
    
    /**
     * @param item
     * @return CacheKey representation used to look up the item in cache
     */
    public static CacheKey getCacheKey(ItemStack item) {
        AttributeStorage storage = AttributeStorage.newTarget(item, ITEM_TRACKER);
        if (storage.getData(null) != null) {
            return new CacheKey(item, UUID.fromString(storage.getData(null)));
        } else {
            UUID id = UUID.randomUUID();
            storage.setData(id.toString());
            item = storage.getTarget();
            return new CacheKey(item, id);
        }
    }
    
    /**
     * Gets the Item Tracker ID if present
     * @param item
     * @return Item Tracker ID, or null
     */
    public static UUID getItemTrackerId(ItemStack item) {
        AttributeStorage storage = AttributeStorage.newTarget(item,ITEM_TRACKER);
        if(storage.getData(null) != null) {
            return UUID.fromString(storage.getData(null));
        } else {
            return null;
        }
    }
    
    /**
     * Gets data stored under a specific id
     * @param id
     * @param item
     * @return String representation of data, or null if no data
     */
    public static String getData(UUID id, ItemStack item) {
        AttributeStorage storage = AttributeStorage.newTarget(item, id);
        return storage.getData(null);
    }
    
    /**
     * Internal utility method for storing module data - do not use
     * @param id
     * @param data
     * @param item
     */
    public static void writeData(UUID id, String data, ItemStack item) {
        AttributeStorage storage = AttributeStorage.newTarget(item, id);
        storage.setData(data);
        if(storage.getTarget() != item) {
            throw new IllegalArgumentException("Item target changed during NBT Write - Are you sure you wrote to a Crafty Item?");
        }
    }

}
