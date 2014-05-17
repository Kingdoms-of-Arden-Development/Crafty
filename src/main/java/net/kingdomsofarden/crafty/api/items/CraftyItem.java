package net.kingdomsofarden.crafty.api.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.CacheKey;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;
import net.kingdomsofarden.crafty.internals.ItemManager;
import net.kingdomsofarden.crafty.internals.NBTUtil;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents an {@link ItemStack} tracked by the Crafty API. To retrieve an instance,
 * use {@link ItemManager#getCraftyItem(ItemStack)}.<br>
 * <br>
 * Direct instantiation of this class is <b>not</b> supported.<br>
 * <br>
 * This object supports the following operations
 * <ul>
 *   <li>Retrieval of {@link Module} stored on the item</li>
 *   <li>Addition of new Modules to store on the item</li>
 *   <li>Triggering a save/write of Module data, the Module list, and Module-defined lore data</li>
 * </ul>
 * <br>
 * A {@link CraftyItem} loaded from cache will expire after 15 minutes since last access, at which point
 * the save/write method is called automatically. 
 * 
 * @author Andrew2060
 */
public final class CraftyItem {
    
    private final UUID itemIdentifier;
    
    private ItemStack item;
    private HashMap<UUID,Module> modules;
    private Crafty plugin;
        
    public CraftyItem(CacheKey key, Crafty plugin) {
        this.plugin = plugin;
        this.item = key.getItem();
        this.itemIdentifier = key.getItemUuid();
        String moduleParse = plugin.getItemManager().getModules(item);
        this.modules = new HashMap<UUID,Module>();
        if(moduleParse != null) {
            String[] moduleParsed = moduleParse.split(":");
            ModuleRegistrar registrar = plugin.getModuleRegistrar();
            ConfigurationManager config = plugin.getConfigurationManager();
            for(String idString : moduleParsed) {
                try {
                    UUID moduleId = UUID.fromString(idString);
                    //Handle migrations is necessary
                    UUID migratedId = config.getMigratedModule(moduleId);
                    while(migratedId != null) {
                        moduleId = migratedId;
                        migratedId = config.getMigratedModule(migratedId);
                    }
                    Module modToAdd = registrar.getModule(migratedId, item);
                    if(modToAdd != null) {
                        this.modules.put(migratedId, modToAdd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
    
    /**
     * All items being tracked by Crafty are assigned a persistant unique identifier that allows
     * for tracking of that specific item.
     * 
     * @return A UUID representing the item's tracking ID
     */
    public UUID getItemTrackerId() {
        return this.itemIdentifier;
    }
    
    /**
     * Gets the item represented by the CraftyItem
     * @return An ItemStack representation of this CraftyItem
     */
    public ItemStack getItem() {
        return this.item;
    }
    
    /**
     * Gets a map representation of the modules currently stored on this item
     * @return An immutable map of the {@link Module}'s registered UUID to
     * a Module instance specific to that item.
     */
    public Map<UUID, Module> getModules() {
        return Collections.unmodifiableMap(this.modules);
    }
    
    /**
     * Gets a Module instance associated with this item by name
     * @param name - The name of the module to get
     * @return The module instance attached to this item with the parameter name, or null if not found
     */
    public Module getModule(String name) {
        return getModule(this.plugin.getModuleRegistrar().getModuleUuid(name));
    }
    
    /**
     * Gets a Module instance associated with this item by UUID
     * @param id - The unique identifier of the module to get
     * @return The module instance attached to this item with the parameter UUID, or null if not found
     */
    public Module getModule(UUID id) {
        if(id == null) {
            return null;
        } else {
            return this.modules.get(id);
        }
    }
    
    /**
     * Adds a new module to the item by name by calling a module's createNewModule method. 
     * The module must have been registered using {@link ModuleRegistrar#registerModule(String, UUID, Class)} 
     * prior to use.<br> 
     * <br>
     * Successful completion will automatically trigger a save/write of all current data. 
     * @param name - The name of the module to add
     * @param initArgs - Any initialization data necessary for creation of a new module instance
     * that is upcasted to {@link Object} and passed to the module's createNewModule method
     */
    public void addModule(String name, Object... initArgs) {
        UUID id = this.plugin.getModuleRegistrar().getModuleUuid(name);
        this.addModule(id, initArgs);
        return;
    }
    
    /**
     * Adds a new module to the item by UUID by calling a module's createNewModule method.
     * The Module must have been registered using {@link ModuleRegistrar#registerModule(String, UUID, Class)} 
     * prior to use.<br> 
     * <br>
     * Succesful completion will automatically trigger a save/write of all current data.
     * @param id The UUID representation of the module to add
     * @param initArgs - Any initialization data necessary for creation of a new module instance
     * that is upcasted to {@link Object} and passed to the module's createNewModule method
     */
    public void addModule(UUID id, Object... initArgs) {
        if(id == null) {
            return;
        }
        Module m = this.plugin.getModuleRegistrar().createModule(id, this.item, initArgs);
        if(m != null) {
            this.modules.put(id, m);
        }
        this.updateItem();
    }
    
    /**
     * Checks whether a given item has a module with the given name, slower than 
     * lookup by UUID
     * @param name The name of the Module to look up
     * @return True if the module exists on this item, false otherwise
     */
    public boolean hasModule(String name) {
        return hasModule(this.plugin.getModuleRegistrar().getModuleUuid(name));
    }
    
    /**
     * Checks whether a given item has a module with the given UUI
     * @param id The UUID of the Module to look up
     * @return True if the module exists on this item, false otherwise
     */
    public boolean hasModule(UUID id) {
        if(id == null) {
            return false;
        } else {
            return this.modules.containsKey(id);
        }
    }
    
    /**
     * Triggers a save/write of all module data on the item and updates lore 
     * based on results of {@link Module#getLoreSection()} and the configuration
     * settings for module ordering.
     */
    public void updateItem() {
        List<String> lore = plugin.getConfigurationManager().getOrderedLore(this.modules);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore); 
        StringBuilder uuidStringBuilder = new StringBuilder();
        boolean write = false; // Used for determining whether a colon delimiter needs to be prepended
        for(Module m : this.modules.values()) {
            if(write) {
                uuidStringBuilder.append(":");
            } else {
                write = true;
            }
            uuidStringBuilder.append(m.getIdentifier().toString());
            String store = m.serialize();
            if(store != null) {
                NBTUtil.writeData(m.getIdentifier(), store, this.item);
            } else {
                continue;
            }
        }
    }
    

}
