/**
 * Stores custom item settings as statically accessible values
 */

package net.kingdomsofarden.crafty.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.CacheKey;
import net.kingdomsofarden.crafty.internals.ItemCache;
import net.kingdomsofarden.crafty.internals.NBTUtil;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

/**
 * Handles conversion of existing ItemStacks to a supported ItemStack format, 
 * checking for whether an ItemStack is tagged as a custom item,
 * as well as getting an actual {@link CraftyItem} from the given ItemStack
 * 
 * @author Andrew2060
 *
 */
public final class ItemManager {
     
    private static final UUID MODULE_STORAGE_KEY = UUID.fromString("65bd4610-a0d8-11e3-a5e2-0800200c9a66");
    private ItemCache cache;
    private final Class<?> craftItemStackClass;
    private final Constructor<?> craftItemStackCtor;
    
    
    /**
     * Instantiation is handled by the API plugin. Use {@link Crafty#getItemManager()} to retrieve an instance.
     * 
     * @param plugin - The plugin instance for internal usage, do not instantiate directly
     */
    public ItemManager(Crafty plugin) {
        try {
            this.cache = new ItemCache(plugin);
            this.craftItemStackClass = Class.forName(this.getPackageName() + ".inventory.CraftItemStack");
            this.craftItemStackCtor = craftItemStackClass.getDeclaredConstructor(ItemStack.class);
            this.craftItemStackCtor.setAccessible(true);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Bukkit Version not Supported!", e);
        }
    }
    
    private String getPackageName() {
        Server server = Bukkit.getServer();
        String name = server != null ? server.getClass().getPackage().getName() : null;
        
        if (name != null && name.contains("craftbukkit")) {
            return name;
        } else {
            return "org.bukkit.craftbukkit.v1_7_R3"; 
        }
    } 
    
    /**
     * Gets a colon delimited string representation of all modules on the item
     * @param item
     * @return A string representation of the modules on the item, or null if none exists
     */
    public String getModules(ItemStack item) {
        return NBTUtil.getData(MODULE_STORAGE_KEY, item);
    }
    
    /**
     * Writes a colon delimited string of module UUIDs to the item
     * @param modules 
     * @param item
     */
    public void saveModules(String modules, ItemStack item) {
        NBTUtil.writeData(MODULE_STORAGE_KEY, modules, item);
        return;
    }
    
    /**
     * Checks whether a given {@link ItemStack} is tagged as a custom item
     * @param item - The item to check
     * @return true if this is a custom item, false otherwise
     */
    public boolean isCraftyItem(ItemStack item) {
        return this.craftItemStackClass.isAssignableFrom(item.getClass()) ;
    }
    
    /**
     * Marks a {@link ItemStack} as a custom item supported by this API.
     * The converted ItemStack might be different from the parameter ItemStack   
     *
     * @param item - The item to convert
     * @return The converted itemstack, or the same itemstack if it is already tagged as a custom item,
     * or null if instantiation fails
     */
    public ItemStack createCraftyItem(ItemStack item) {
        if(isCraftyItem(item)) {
            return item;
        } else { 
            try {
                return (ItemStack) craftItemStackCtor.newInstance(item);
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    
    /**
     * Gets a {@link CraftyItem} from the parameter itemstack provided it has been converted
     * to a CraftyItem already
     * @param item - The item to get the {@link CraftyItem} for
     * @return A {@link CraftyItem} containing a set of modules as well as various utility methods
     */
    public CraftyItem getCraftyItem(ItemStack item) {
        if(!this.isCraftyItem(item)) {
            return null;
        }
        try {
            return cache.get(NBTUtil.getCacheKey(item));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Causes a refresh of the last access time in cache for a specified item
     * @param key CacheKey representing the item to refresh
     */
    public void refresh(CacheKey key) {
        try {
            this.cache.get().get(key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }        
    }
    
}
