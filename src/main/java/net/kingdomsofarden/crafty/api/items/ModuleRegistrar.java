package net.kingdomsofarden.crafty.api.items;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.NBTUtil;

import org.bukkit.inventory.ItemStack;

/**
 * The {@link ModuleRegistrar} handles registration and instantiation of the {@link Module} object.
 * Modules must be registered with the registrar prior to use or they will not be recognized and
 * any values stored will be ignored. Items with an unrecognized UUID will first defer to the 
 * migration configuration in an attempt to find a matching replacement, or will remove that UUID
 * if no migration is found.<br>  
 * <br>
 * Registration is done by calling the method {@link #registerModule(String, UUID, Class)}
 * 
 * @author Andrew2060
 * 
 */
public final class ModuleRegistrar {

    private Crafty plugin;
    private Map<UUID, Class<? extends Module>> idToClassMap;
    private Map<UUID, String> idToNameMap;
    private Map<String, UUID> nameToIdMap;
    
    public ModuleRegistrar(Crafty plugin) {
        this.plugin = plugin;
        this.idToClassMap = new HashMap<UUID, Class<? extends Module>>();
        this.idToNameMap = new HashMap<UUID, String>();
        this.nameToIdMap = new HashMap<String, UUID>();
    }

    /**
     * Registers a module with this registrar, allowing for retrieval/saving of this data to an item
     * @param name The name of the module
     * @param id A {@link UUID} representing this module
     * @param moduleClazz The class of the Module to register
     * @return whether registration was successful
     */
    public boolean registerModule(String name, UUID id, Class<? extends Module> moduleClazz) {
        if (idToClassMap.containsKey(id)) {
            if (idToClassMap.get(id).getClass().getName().equals(moduleClazz.getName())) {
                UUID nameMapping = nameToIdMap.get(name);
                if (nameMapping != null && nameMapping.equals(id)) {
                    return true; // Duplicate registration of the same class, fail silently
                } else if(nameMapping == null) {
                    nameToIdMap.put(name, id); // Missing Name->ID mapping
                    idToNameMap.put(id, name);
                    return true; // ID->Class map exists and Name->ID Map now exists, return
                }
            }
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " with UUID " + id.toString() 
                    + " which duplicates a preexisting registration for " 
                    + idToClassMap.get(id).getName());
        }
        if (nameToIdMap.containsKey(name)) {
            if (!nameToIdMap.get(name).equals(id)) {
                throw new UnsupportedOperationException("An attempt was made to register module "
                        + moduleClazz.getName() + " with name " + name
                        + " which duplicates a preexisting registration for "
                        + idToClassMap.get(nameToIdMap.get(id)).getName());
            }
        }
        try {
            moduleClazz.getMethod("deserialize", Crafty.class, String.class, ItemStack.class);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " which does not implement the required method "
                    + "public static Module deserialize(Crafty plugin, String data, ItemStack item) ");
        } catch (Exception e) {
            throw new RuntimeException("An unknown error occured when attempting to check for "
                    + "the presence of a deserialization method in "
                    + moduleClazz.getName() , e);
        }
        try {
            moduleClazz.getMethod("createNewModule", Crafty.class, ItemStack.class, Object[].class);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("An attempt was made to register module " 
                    + moduleClazz.getName() + " which does not implement the required method "
                    + "public static Module createNewModule(Crafty plugin, ItemStack item, Object... initArgs)");
        } catch (Exception e) {
            throw new RuntimeException("An unknown error occured when attempting to check for "
                    + "the presence of a new module instantiation method in "
                    + moduleClazz.getName() , e);
        }
        idToClassMap.put(id, moduleClazz);
        nameToIdMap.put(name, id);
        idToNameMap.put(id, name);
        return true;
    }
    
    /**
     * Loads a module instance from the provided item - slightly slower than loading a module
     * by UUID
     *  
     * @param name Name of the module to load
     * @param item The ItemStack to load the module's data from
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public Module getModule(String name, ItemStack item) {
        UUID id = nameToIdMap.get(name);
        return getModule(idToClassMap.get(id), name, id, item);
    }
    
    /**
     * Loads a module instance from the provided item 
     * @param id The UUID of the module to load
     * @param item The ItemStack to load the module's data from
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public Module getModule(UUID id, ItemStack item) {
        return getModule(idToClassMap.get(id), idToNameMap.get(id), id, item);
    }
    
    /**
     * Allows for faster lookup of the ID of a registered module by name compared to getting the whole module
     * @param name The name to look up
     * @return The ID of the module, or null if the module does not exist
     */
    public UUID getModuleUuid(String name) {
        return this.nameToIdMap.get(name);
    }
    
    /**
     * Allows for faster lookup of the name of a registered module by id compared to getting the whole module
     * @param id The id to look up
     * @return The name of the module, or null if the module does not exist
     */
    public String getModuleName(UUID id) {
        return this.idToNameMap.get(id);
    }
    
    private <T extends Module> T getModule(Class<? extends Module> clazz, String name, UUID id, ItemStack item) {
        if(clazz == null || name == null || id == null || item == null) {
            return null;
        }
        try {
            Method m = clazz.getMethod("deserialize", Crafty.class, String.class, ItemStack.class);
            String data = NBTUtil.getData(id, item);
            @SuppressWarnings("unchecked")
            T mod = (T) m.invoke(null, plugin, data, item);
            if(mod == null) {
                return null;
            }
            Field uuidField = clazz.getDeclaredField("identifier");
            uuidField.setAccessible(true);
            uuidField.set(mod, id);
            Field nameField = clazz.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(mod, name);
            return mod;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } 
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
    public Module createModule(String name, ItemStack item, Object...initArgs) {
        UUID id = nameToIdMap.get(name);
        return createModule(idToClassMap.get(id), name, id, item, initArgs);
    }
    
    /**
     * Creates a module instance from the provided item. Do not call directly 
     * - use {@link CraftyItem#addModule(String, Object...)} or {@link CraftyItem#addModule(UUID, Object...)}
     * @param id The UUID of the module to load
     * @param item The ItemStack to load the module's data from
     * @param initArgs Initialization data for the createNewModule method
     * @return The loaded module, or null if for some reason the module failed to load or does not exist
     */
    public Module createModule(UUID id, ItemStack item, Object...initArgs) {
        return createModule(idToClassMap.get(id), idToNameMap.get(id), id, item, initArgs);
    }
    private <T extends Module> T createModule(Class<? extends Module> clazz, String name, UUID id, ItemStack item, Object...initArgs) {
        if(clazz == null || name == null || id == null || item == null) {
            return null;
        }
        try {
            Method m = clazz.getMethod("createNewModule", Crafty.class, ItemStack.class, Object[].class);
            @SuppressWarnings("unchecked")
            T mod = (T) m.invoke(null, plugin, item, initArgs);
            if(mod == null) {
                return null;
            }
            Field uuidField = clazz.getDeclaredField("identifier");
            uuidField.setAccessible(true);
            uuidField.set(mod, id);
            Field nameField = clazz.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(mod, name);
            return mod;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } 
    }
    
}
