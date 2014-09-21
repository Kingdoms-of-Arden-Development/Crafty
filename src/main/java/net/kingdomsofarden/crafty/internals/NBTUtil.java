package net.kingdomsofarden.crafty.internals;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NBTUtil {

     static final UUID CRAFTY_ITEM_TRACKER = UUID.fromString("198d8160-c487-11e3-9c1a-0800200c9a66");
     static NBTUtil instance = null;

    /**
     * @param item
     * @return CacheKey representation used to look up the item in cache
     */
    CacheKey getCacheKey(Object item);

    /**
     * @param item
     * @return Item Tracker ID of this item, or null
     */
    UUID getItemTrackerId(Object item);

    /**
     * @param item The item to check for data on
     * @param id   The UUID of the NBT data tag to check for
     * @return Whether the given item has any data stored under the given UUID
     */
    boolean hasData(Object item, UUID id);

    /**
     * Gets data stored under a specific id
     *
     * @param id   The UUID of the NBT data tag to get
     * @param item The item to get the NBT data from
     * @return String representation of data, or null if no data
     */
    String getData(UUID id, Object item);

    /**
     * Internal utility method for storing module data - can be used to directly write to NBT
     * although said data will not be tracked by Crafty
     *
     * @param id   The UUID of the NBT to save under
     * @param data The data (this will be saved as the data tag's name)
     * @param item The item to save the NBT on
     */
    void writeData(UUID id, String data, Object item);

    /**
     * Writes the given vanilla attributes to an item - used by modules to store vanilla
     * attribute data although you are free to store your own (under a different UUID obviously)
     *
     * @param values
     * @param item
     */
    void writeVanillaAttributes(Collection<AttributeInfo> values, Object item);

    /**
     * Writes the given list of strings to lore - this is used by module write
     * NOT FOR EXTERNAL USE - Lore WILL be overwritten if modules with lore data are present
     * @param lore The lore to write
     * @param item
     */
    void setLore(List<String> lore, Object item);
}