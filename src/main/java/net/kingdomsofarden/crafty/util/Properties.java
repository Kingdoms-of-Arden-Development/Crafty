/**
 * Stores configuration settings as statically accessible values
 */

package net.kingdomsofarden.crafty.util;

import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.api.items.CraftyItem;

public class Properties {
    
    
    public static final UUID PluginUUID = UUID.fromString("65bd4610-a0d8-11e3-a5e2-0800200c9a66");
    public static final UUID craftyVersion = UUID.fromString("9e914590-a0f1-11e3-a5e2-0800200c9a66");
        
    private static Map<String,Integer> itemToId;
    private static Map<Integer,String> idToItem;

    
    public static Map<String,Integer> getItemToId() {
        return itemToId;
    }

    public static Map<Integer,String> getIdToItem() {
        return idToItem;
    }

    public static int getItemId(CraftyItem item) {
        String lookup = item.getClass().toString();
        Integer id = itemToId.get(lookup);
        if(id == null) {
            //TODO: Handle unregistered ID insertion
        }
        return id;
    }

     
}
