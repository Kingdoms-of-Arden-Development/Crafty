package net.kingdomsofarden.crafty.api.items;

import java.util.List;
import java.util.UUID;

/**
 * Represents a module that can be attached to an item, acting as persistant metadata identified via a UUID.<br>
 * <br>
 * A module supports the following operations: <br>
 * <ul>
 *     <li>Modifying Lore</li>
 *     <li>Associating data with a specific {@link CraftyItem} by adding the module to the item's stored module list</li>
 *     <li>Persistance of the aforementioned data using NBT Tags and String serialization/deserialization</li>
 * </ul>
 * Modules must be registered with the {@link ModuleRegistrar} prior to use<br>
 * <br>
 * Instantiation is done via deserialize(Crafty,String,ItemStack), or through createNewModule(Crafty,ItemStack,Object...)
 * meaning that any constructors called by the API will have been called through said two methods. 
 * In the event that the module stores no data, or no existing data is stored on the item at the time of the instantiation attempt, a parameter value of null will be provided
 * to the method.<br>
 * 
 * Modules must also implement the following static methods:<br>
 * <ul>
 * <li>{@code public static Module createNewModule(Crafty plugin, ItemStack item, Object... initArgs)}
 *     <ul>
 *     <li>Used when calling {@link CraftyItem#addModule(String, Object...)} or {@link CraftyItem#addModule(UUID, Object...)}</li>
 *     <li><b>plugin</b> - The Item API Plugin Instance</li>
 *     <li><b>item</b> - The item that the module is to be loaded onto</li>
 *     <li><b>initArgs</b> - Provided during the addModule call, an Object array that can be downcasted into
 *         whatever information is necessary to create a new Module instance</li>
 *     <li><b>Returns</b> a new Module instance created from the initArgs to be applied to the item</li>
 *     </ul>
 * <li>{@code public static Module deserialize(Crafty plugin, String data, ItemStack item)}
 *     <ul>
 *     <li>Used to load a Module instance on an item that is marked as having said module</li>
 *     <li><b>plugin</b> - The Item API Plugin Instance</li>
 *     <li><b>data</b> - A string representation of any of the data stored under the module's UUID on this item, or
 *         null if none</li>
 *     <li><b>item</b> - The item that the module is to be loaded from</li>
 *     <li><b>Returns</b> a Module object loaded from data stored from the string created by calling {@link #serialize()}</li>
 *     </ul>
 * </li>
 * <br>
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
