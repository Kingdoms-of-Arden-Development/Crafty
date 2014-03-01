/**
 * Stores custom item settings as statically accessible values
 */

package net.kingdomsofarden.crafty.util;

import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.api.items.CraftyItem;

public class ItemManager {
     
    public static final UUID PluginUUID = UUID.fromString("65bd4610-a0d8-11e3-a5e2-0800200c9a66");
    //To be changed only with breaking change to storage
    public static final UUID craftyVersion = UUID.fromString("9e914590-a0f1-11e3-a5e2-0800200c9a66"); 
        
    private static Map<String,UUID> identifierNameMap;
    private static Map<UUID,CraftyItem> itemIdentifierMap;

    
    public static UUID getUUID(String name) {
        return identifierNameMap.get(name);
    }

    public static CraftyItem getItem(UUID id) {
        return itemIdentifierMap.get(id);
    }

    public static UUID getItemId(CraftyItem item) {
        String lookup = item.getClass().toString();
        UUID id = identifierNameMap.get(lookup);
        if(id == null) {
            //TODO: Handle unregistered ID insertion
        }
        return id;
    }

     
}
