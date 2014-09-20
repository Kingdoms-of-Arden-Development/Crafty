package net.kingdomsofarden.crafty.internals;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public interface NBTUtil {

     static final UUID CRAFTY_ITEM_TRACKER = UUID.fromString("198d8160-c487-11e3-9c1a-0800200c9a66");

    /**
     * @param item
     * @return CacheKey representation used to look up the item in cache
     */
    CacheKey getCacheKey(ItemStack item);

    /**
     * @param item
     * @return Item Tracker ID, or null
     */
    UUID getItemTrackerId(ItemStack item);

    /**
     * @param item The item to check for data on
     * @param id   The UUID of the NBT data tag to check for
     * @return Whether the given item has any data stored under the given UUID
     */
    boolean hasData(ItemStack item, UUID id);

    /**
     * Gets data stored under a specific id
     *
     * @param id   The UUID of the NBT data tag to get
     * @param item The item to get the NBT data from
     * @return String representation of data, or null if no data
     */
    String getData(UUID id, ItemStack item);

    /**
     * Internal utility method for storing module data - can be used to directly write to NBT
     * although said data will not be tracked by Crafty
     *
     * @param id   The UUID of the NBT to save under
     * @param data The data (this will be saved as the data tag's name)
     * @param item The item to save the NBT on
     */
    void writeData(UUID id, String data, ItemStack item);

    /**
     * Writes the given vanilla attributes to an item - used by modules to store vanilla
     * attribute data although you are free to store your own (under a different UUID obviously)
     *
     * @param values
     * @param item
     */
    void writeVanillaAttributes(Collection<AttributeInfo> values, ItemStack item);
}