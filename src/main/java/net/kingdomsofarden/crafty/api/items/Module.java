package net.kingdomsofarden.crafty.api.items;

import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a module that can be attached to an item. <br>
 * <br>
 * A module supports the following operations: <br>
 * <ul>
 *     <li>Modifying Lore and Item Names</li>
 *     <li>Associating data with a specific {@link ItemStack} by adding the module to the ItemStack's module list</li>
 *     <li>Persistance of the aforementioned data using NBT Tags and String serialization/deserialization</li>
 * </ul>
 * Modules must be registered with the {@link ModuleRegistrar} prior to use<br>
 * Modules must also implement a deserialization method with the following syntax:<br>
 * {@code public static Module deserialize(String string)}
 * that will return a Module object loaded from data stored as a string created by calling {@link #serialize()},
 * in addition to the abstract methods defined in this class. (A check for this will be done upon registration 
 * with the Module Registrar)<br>
 * <br>
 * Initialization is done via the aforementioned deserialization method, meaning that any constructors called
 * by the API will have been called through said method. In the event that the module stores no data, or no existing
 * data is stored on the ItemStack at the time of the initialization attempt, a parameter value of null will be sent
 * to the method.
 * 
 * @author Andrew2060
 */
public abstract class Module {

    private UUID identifier;
    private String name;
    
    /**
     * Gets the {@link UUID} of this module, the value of which is set upon module registration.
     * Plugins should ensure that the UUID remains consistant over server restarts - i.e. they 
     * should always be registering the same module classes using the same UUID. This is important as
     * the unique identifier is how the API determines what modules are attached to an item.
     * 
     * @return an universally unique identifier identifying the module 
     */
    public final UUID getIdentifier() {
        return this.identifier;
    }
    
    /**
     * Gets the name of this module, the value of which is set upon module registration.
     * Plugins should ensure that the name remains consistant over server restarts - i.e. they 
     * should always be registering the same module classes using the same name. This is important as
     * the name is what is displayed to end users for various configuration options such as the ordering
     * of item lore as well as migration options
     * 
     * @return a String representing a name to use in identifying this module 
     */
    public final String getName() {
        return this.name;
    }
    
    /**
     * Gets a lore section attributed to this module instance.
     * This should reflect whatever data that is stored by the module that needs to be displayed to the player.
     * Ordering of the lore section of modules is determined by the user via configuration and
     * overrides of the order is not supported at this time.
     * 
     * @return A list of strings indexed incrementally representing a description of this module and/or relevent data
     * to be displayed to the end user. If this module does not wish to have a lore section, then returns null.
     */
    public abstract List<String> getLoreSection();
    
    /**
     * Converts the data stored in this module to a writeable format
     * @return A String representation of the data stored in this module instance. If this module does not wish to 
     * store any data, then returns null.
     */
    public abstract String serialize(); 
    
}
