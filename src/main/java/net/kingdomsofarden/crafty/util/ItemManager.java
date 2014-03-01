/**
 * Stores custom item settings as statically accessible values
 */

package net.kingdomsofarden.crafty.util;

import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import net.kingdomsofarden.crafty.api.items.CraftyItem;
import net.kingdomsofarden.crafty.internals.thirdparty.comphoenix.AttributeStorage;

public class ItemManager {
     
    public static final UUID PluginUUID = UUID.fromString("65bd4610-a0d8-11e3-a5e2-0800200c9a66");
        
    private static Map<UUID,CraftyItem> itemIdentifierMap;

    public static CraftyItem getItem(UUID id) {
        return itemIdentifierMap.get(id);
    }
   
    public static AttributeStorage getCraftyItem(ItemStack item) {
        AttributeStorage storage = AttributeStorage.newTarget(item,ItemManager.PluginUUID);
        return storage.getData() != null ? storage : null;
    }
     
}
