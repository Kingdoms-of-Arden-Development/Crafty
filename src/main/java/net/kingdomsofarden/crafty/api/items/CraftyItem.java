package net.kingdomsofarden.crafty.api.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.internals.CraftyAttribute;
import net.kingdomsofarden.crafty.internals.thirdparty.comphoenix.AttributeStorage;
import net.kingdomsofarden.crafty.util.Properties;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public abstract class CraftyItem {
    
    private String displayName = null;
    private List<String> lore = null;
    private Map<Enchantment,Integer> enchants = null;    

    public final UUID itemUniqueId;
    
    public CraftyItem(UUID id) {
        this.itemUniqueId = id;
    }
    
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    
    public void setEnchantments(Map<Enchantment,Integer> enchants) {
        this.enchants = enchants;
    }
    
    public final ItemStack updateItemStack(ItemStack item) {
        
        AttributeStorage storage = AttributeStorage.newTarget(item,Properties.PluginUUID);

        UUID id = Properties.getItemId(this);
        
        String parseable = storage.getData();
        
        CraftyAttribute attrib = null;
        
        if(parseable != null) {
            attrib = CraftyAttribute.fromString(parseable);
            attrib.insert(id);
        } else {
            attrib = new CraftyAttribute(Properties.craftyVersion,id);
        }
        
        storage.setData(attrib.toString());
        
        ItemStack updatedItem = storage.getTarget();
        
        ItemMeta meta = updatedItem.getItemMeta();
        if(displayName != null) {
            meta.setDisplayName(displayName);
        }
        if(lore != null) {
            meta.setLore(lore);
        }
        if(enchants != null) {
            for(Enchantment e : enchants.keySet()) {
                meta.addEnchant(e, enchants.get(e), true);
            }
        }
        updatedItem.setItemMeta(meta);
        
        return updatedItem;
    }

    

}
