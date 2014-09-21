package net.kingdomsofarden.crafty.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.CraftyPlugin;
import net.kingdomsofarden.crafty.internals.NBTUtil;

/**
 * The {@link ModuleRegistrar} handles registration and instantiation of the {@link Module} object.
 * Modules must be registered with the registrar prior to use or they will not be recognized and
 * any values stored will be ignored. Items with an unrecognized UUID will first defer to the 
 * migration configuration in an attempt to find a matching replacement, or will remove that UUID
 * if no migration is found.<br>  
 * <br>
 * Registration is done by calling the method {@link #registerModule(String, UUID, Class)} and should be done
 * during plugin load (prior to plugin enable)
 * 
 * @author Andrew2060
 * 
 */
public final class ModuleRegistrar {

    private CraftyPlugin plugin;
    private Map<UUID, Class<? extends Module>> idToClassMap;
    private Map<UUID, String> idToNameMap;
    private Map<String, UUID> nameToIdMap;

    private boolean registerLock;
    
    public ModuleRegistrar(CraftyPlugin plugin) {
        this.plugin = plugin;
        this.registerLock = false;
        this.idToClassMap = new HashMap<UUID, Class<? extends Module>>();
        this.idToNameMap = new HashMap<UUID, String>();
        this.nameToIdMap = new HashMap<String, UUID>();
    }

    /**
     * Registers a module with this registrar, allowing for retrieval/saving of this data to an item<br>
     * Must be called on plugin load (onLoad()) and before enable (onEnable())
     * @param name The name of the module
     * @param id A {@link UUID} representing this module
     * @param moduleClazz The class of the Module to register
     * @return whether registration was successful
     */
    public boolean registerModule(String name, UUID id, Class<? extends Module> moduleClazz) {
        if (this.registerLock) {
            throw new IllegalStateException("Cannot register module after plugin load: " + moduleClazz.getName());
        }
        if (this.idToClassMap.containsKey(id)) {
            if (this.idToClassMap.get(id).getClass().getName().equals(moduleClazz.getName())) {
                UUID nameMapping = nameToIdMap.get(name);
                if (nameMapping != null && nameMapping.equals(id)) {
                    return true; // Duplicate registration of the same class, fail silently
                } else if (nameMapping == null) {
                    this.nameToIdMap.put(name, id); // Missing Name->ID mapping
                    this.idToNameMap.put(id, name);
                    return true; // ID->Class map exists and Name->ID Map now exists, return
                }
            }
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " with UUID " + id.toString() 
                    + " which duplicates a preexisting registration for " 
                    + this.idToClassMap.get(id).getName());
        }
        if (this.nameToIdMap.containsKey(name)) {
            if (!this.nameToIdMap.get(name).equals(id)) {
                throw new UnsupportedOperationException("An attempt was made to register module "
                        + moduleClazz.getName() + " with name " + name
                        + " which duplicates a preexisting registration for "
                        + this.idToClassMap.get(this.nameToIdMap.get(id)).getName());
            }
        }
        try {
            moduleClazz.getMethod("deserialize", Crafty.class, String.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " which does not implement the required method "
                    + "public static Module deserialize(Crafty plugin, String data, Object item) ");
        } catch (Exception e) {
            throw new RuntimeException("An unknown error occurred when attempting to check for "
                    + "the presence of a deserialization method in "
                    + moduleClazz.getName() , e);
        }
        try {
            moduleClazz.getMethod("createNewModule", Crafty.class, Object.class, Object[].class);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " which does not implement the required method "
                    + "public static Module createNewModule(Crafty plugin, Object item, Object... initArgs)");
        } catch (Exception e) {
            throw new RuntimeException("An unknown error occurred when attempting to check for "
                    + "the presence of a new module instantiation method in "
                    + moduleClazz.getName() , e);
        }
        this.idToClassMap.put(id, moduleClazz);
        this.nameToIdMap.put(name, id);
        this.idToNameMap.put(id, name);
        return true;
    }

    /**
     * @return A map of the UUID and name of all registered modules
     */
    public Map<UUID, String> getRegisteredModules() {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to get all registered modules prior to registration" +
                    " being finished");
        }
        return this.idToNameMap;
    }

    /**
     * Loads a module instance from the provided item - slightly slower than loading a module
     * by UUID
     *  
     * @param name Name of the module to load
     * @param item The ItemStack to load the module's data from
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public <T extends Module> T getModule(String name, Object item) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to get a module prior to registration" +
                    " being finished");
        }
        UUID id = this.nameToIdMap.get(name);
        return getModule(this.idToClassMap.get(id), name, id, item);
    }
    
    /**
     * Loads a module instance from the provided item 
     * @param id The UUID of the module to load
     * @param item The ItemStack to load the module's data from
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public <T extends Module> T getModule(UUID id, Object item) {
        return getModule(this.idToClassMap.get(id), this.idToNameMap.get(id), id, item);
    }
    
    /**
     * Allows for faster lookup of the ID of a registered module by name compared to getting the whole module
     * @param name The name to look up
     * @return The ID of the module, or null if the module does not exist
     */
    public UUID getModuleUuid(String name) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to get a module UUID prior to registration" +
                    " being finished");
        }
        return this.nameToIdMap.get(name);
    }
    
    /**
     * Allows for faster lookup of the name of a registered module by id compared to getting the whole module
     * @param id The id to look up
     * @return The name of the module, or null if the module does not exist
     */
    public String getModuleName(UUID id) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to get a module name prior to registration" +
                    " being finished");
        }
        return this.idToNameMap.get(id);
    }
    
    /**
     * Creates a module instance from the provided item - slightly slower than creating a module
     * by UUID. Do not call directly - use {@link CraftyItem#addModule(String, Object...)} or 
     * {@link CraftyItem#addModule(UUID, Object...)}
     *  
     * @param name Name of the module to create
     * @param item The ItemStack that will have a module applied to it.
     * @param initArgs Initialization data for the createNewModule method
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public <T extends Module> T createModule(String name, Object item, Object...initArgs) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to create a module prior to registration" +
                    " being finished");
        }
        UUID id = this.nameToIdMap.get(name);
        return createModule(this.idToClassMap.get(id), name, id, item, initArgs);
    }
    
    /**
     * Creates a module instance from the provided item. Do not call directly 
     * - use {@link CraftyItem#addModule(String, Object...)} or {@link CraftyItem#addModule(UUID, Object...)}
     * @param id The UUID of the module to load
     * @param item The ItemStack to load the module's data from
     * @param initArgs Initialization data for the createNewModule method
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public <T extends Module> T createModule(UUID id, Object item, Object...initArgs) {
        return createModule(this.idToClassMap.get(id), idToNameMap.get(id), id, item, initArgs);
    }
    
    /**
     * Generates a new module from a specified string 
     * @param name Name of module
     * @param data String representation of module's data, must be equivalent to that given by {@link Module#serialize()}
     * @param item The item that the module is hypothetically applied to (might be used by some modules)
     * @return A module instance created from the given data - note that the module is NOT attached to the item by this method
     */
    public <T extends Module> T createFromData(String name, String data, Object item) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to create a module prior to registration" +
                    " being finished");
        }
        UUID id = this.nameToIdMap.get(name);
        if (id == null) {
            return null;
        }
        return createFromData(this.idToClassMap.get(id), name, id, item, data);
    }
    
    /**
     * Generates a new module from a specified string 
     * @param id UUID of module
     * @param data String representation of module's data, must be equivalent to that given by {@link Module#serialize()}
     * @param item The item that the module is hypothetically applied to (might be used by some modules)
     * @return A module instance created from the given data - note that the module is NOT attached to the item by this method
     */
    public <T extends Module> T createFromData(UUID id, String data, Object item) {
        if (!this.registerLock) {
            throw new IllegalStateException("An attempt was made to create a module prior to registration" +
                    " being finished");
        }
        String name = this.idToNameMap.get(id);
        if (name == null) {
            return null;
        }
        return createFromData(this.idToClassMap.get(id), name, id, item, data);
    }
    
    // Private utility methods
    
    private <T extends Module> T getModule(Class<? extends Module> clazz, String name, UUID id, Object item) {
        if (clazz == null || name == null || id == null || item == null) {
            return null;
        }
        try {
            Method m = clazz.getMethod("deserialize", Crafty.class, String.class, Object.class);
            String data = NBTUtil.instance.getData(id, item);
            @SuppressWarnings("unchecked")
            T mod = (T) m.invoke(null, this.plugin, data, item);
            if (mod == null) {
                return null;
            }
            mod.setIdentifier(id);
            mod.setName(name);
            return mod;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    private <T extends Module> T createFromData(Class<? extends Module> clazz, String name, UUID id, Object item, String data) {
        if (clazz == null || name == null || id == null || item == null) {
            return null;
        }
        try {
            Method m = clazz.getMethod("deserialize", Crafty.class, String.class, Object.class);
            @SuppressWarnings("unchecked")
            T mod = (T) m.invoke(null, this.plugin, data, item);
            if (mod == null) {
                return null;
            }
            mod.setIdentifier(id);
            mod.setName(name);
            return mod;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    private <T extends Module> T createModule(Class<? extends Module> clazz, String name, UUID id, Object item, Object...initArgs) {
        if (clazz == null || name == null || id == null || item == null) {
            return null;
        }
        try {
            Method m = clazz.getMethod("createNewModule", Crafty.class, Object.class, Object[].class);
            @SuppressWarnings("unchecked")
            T mod = (T) m.invoke(null, this.plugin, item, initArgs);
            if (mod == null) {
                return null;
            }
            mod.setIdentifier(id);
            mod.setName(name);
            return mod;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    
    
}
