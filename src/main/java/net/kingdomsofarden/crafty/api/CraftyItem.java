package net.kingdomsofarden.crafty.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.CraftyPlugin;
import net.kingdomsofarden.crafty.internals.BukkitNBTUtil;
import net.kingdomsofarden.crafty.internals.CacheKey;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;
import net.kingdomsofarden.crafty.internals.NBTUtil;

/**
 * Represents an Item tracked by the Crafty API. To retrieve an instance,
 * use {@link ItemManager#getCraftyItem(Object)}.<br>
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
    
    private Object item;
    private HashMap<UUID,Module> modules;
    private CraftyPlugin plugin;

        
    public CraftyItem(CacheKey key, CraftyPlugin plugin) {
        this.plugin = plugin;
        this.item = key.getItem();
        this.itemIdentifier = key.getItemUuid();
        String moduleParse = plugin.getItemManager().getModules(item);
        this.modules = new HashMap<UUID,Module>();
        if (moduleParse != null) {
            String[] moduleParsed = moduleParse.split(":");
            ModuleRegistrar registrar = plugin.getModuleRegistrar();
            ConfigurationManager config = plugin.getConfigurationManager();
            for (String idString : moduleParsed) {
                try {
                    UUID moduleId = UUID.fromString(idString);
                    //Handle migrations if necessary
                    UUID migratedId = config.getMigratedModule(moduleId);
                    while (migratedId != null) {
                        moduleId = migratedId;
                        migratedId = config.getMigratedModule(migratedId);
                    }
                    Module modToAdd = registrar.getModule(moduleId, item);
                    if (modToAdd != null) {
                        this.modules.put(moduleId, modToAdd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        for (Module mod : this.modules.values()) {
            mod.postLoad(this);
        }
        this.updateItem();
    }
    
    /**
     * All items being tracked by Crafty are assigned a persistent unique identifier that allows
     * for tracking of that specific item.
     * 
     * @return A UUID representing the item's tracking ID
     */
    public UUID getItemTrackerId() {
        return this.itemIdentifier;
    }
    
    /**
     * Gets the item represented by the CraftyItem
     * @return An implementation-specific representation of this CraftyItem
     */
    public Object getItem() {
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
        if (id == null) {
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
     * Successful completion will not automatically trigger a save/write of all current data. Make sure to call
     * {@link #updateItem()}!
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
     * Successful completion will not automatically trigger a save/write of all current data. Make sure to call
     * {@link #updateItem()}!
     * @param id The UUID representation of the module to add
     * @param initArgs - Any initialization data necessary for creation of a new module instance
     * that is upcasted to {@link Object} and passed to the module's createNewModule method
     */
    public void addModule(UUID id, Object... initArgs) {
        if (id == null) {
            return;
        }
        Module m = this.plugin.getModuleRegistrar().createModule(id, this.item, initArgs);
        if (m != null) {
            this.modules.put(id, m);
            m.postLoad(this);
        }
    }
    
    /**
     * Adds a given module to the CraftyItem - intended to be used in conjunction with 
     * {@link ModuleRegistrar#createFromData(String, String, Object)} which does not
     * automatically attach the returned module to the item 
     * @param mod The module to add
     */
    public void addModule(Module mod) {
        if (mod != null) {
            this.modules.put(mod.getIdentifier(), mod);
            mod.postLoad(this);
        } else {
            throw new IllegalArgumentException("The supplied module cannot be null!");
        }
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
        if (id == null) {
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
        StringBuilder uuidStringBuilder = new StringBuilder();
        boolean write = false; // Used for determining whether a colon delimiter needs to be prepended
        for (Module m : this.modules.values()) {
            if (m == null) {
                Crafty.getInstance().getLogger().log(Level.SEVERE,
                        "Null mod for whatever reason was attempted to be written...skipping!") ;
                continue;
            }
            try {
                if (write) {
                    uuidStringBuilder.append(":");
                } else {
                    write = true;
                }
                UUID id = m.getIdentifier();
                if (id == null) {
                    System.out.println("invalid - id is null for " + m.getClass().getName());
                    continue;
                }
                uuidStringBuilder.append(m.getIdentifier().toString());
                String store = m.serialize();
                if (store != null) {
                    NBTUtil.instance.writeData(m.getIdentifier(), store, this.item);
                } else {
                    continue;
                }
                NBTUtil.instance.writeVanillaAttributes(m.getVanillaAttributes().values(), this.item);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error serializing " + m.getClass().getName());
                e.printStackTrace();
                continue;
            }
        }
        List<String> lore = plugin.getConfigurationManager().getOrderedLore(this.modules);
        NBTUtil.instance.setLore(lore, item);
        this.plugin.getItemManager().saveModules(uuidStringBuilder.toString(), item);
    }
    
    /**
     * Removes a module matching the parameter module's UUID from the mapping
     * @param module The module to remove
     * @return True if successful, false if not found
     */
    public boolean removeModule(Module module) {
        return this.removeModule(module.getIdentifier());
    }
    
    /**
     * Removes a module matching the parameter name from the mapping
     * @param name The name of module to remove
     * @return True if successful, false if not found
     */
    public boolean removeModule(String name) {
        UUID map = this.plugin.getModuleRegistrar().getModuleUuid(name);
        if (map == null) {
            return false;
        } else {
            return this.removeModule(map);
        }
    }
    
    /**
     * Removes a module matching the parameter module's UUID from the mapping
     * @param id The UUID of the module to remove
     * @return True if successful, false if not found
     */
    public boolean removeModule(UUID id) {
        boolean flag = this.modules.remove(id) != null;
        if (flag) {
            this.updateItem();
        }
        return flag;
    }
    
    /**
     * Updates the reference implementation Item used by this CraftyItem. Called by cache when it detects that
     * the referenced item has changed - Not intended to be called externally to the API plugin
     * @param item The updated item reference
     */
    public void setItem(Object item) {
        this.item = item;
    }


}
