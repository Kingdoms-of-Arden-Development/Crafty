package net.kingdomsofarden.crafty.api.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.internals.CraftyAttribute;
import net.kingdomsofarden.crafty.internals.ItemCache;
import net.kingdomsofarden.crafty.internals.thirdparty.comphoenix.AttributeStorage;
import net.kingdomsofarden.crafty.util.ItemManager;

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
    
    public final boolean isInterested(ItemStack item) {
        AttributeStorage storage = ItemManager.getCraftyItem(item);
        CraftyAttribute attrib;
        if(storage != null) {
            attrib = ItemManager.getCache().getAttribute(item);
        } else {
            attrib = null;
        }
        if(attrib == null) {
            if(storage == null) {
                return false;
            } else {
                attrib = CraftyAttribute.fromString(item,storage.getData());
            }
        } 
        
        if(attrib.contains(this.itemUniqueId)) {
            return true;
        } else {
            return false;
        }
    }
    
    public final ItemStack expressInterest(ItemStack item) {
        ItemCache cache = ItemManager.getCache();
        AttributeStorage storage = ItemManager.getCraftyItem(item);
        CraftyAttribute attrib;
        boolean wasCached = false;
        
        if(storage != null) {
            attrib = cache.getAttribute(item);
            wasCached = true;
        } else {
            attrib = null;
        }
        String parseable = storage.getData();
                
        if(parseable != null) {
            attrib = CraftyAttribute.fromString(item, parseable);
            attrib.insert(itemUniqueId);
        } else {
            attrib = new CraftyAttribute(item, itemUniqueId);
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
        
        if(wasCached) {
            cache.invalidateItemStack(item);
            cache.cache(updatedItem);
        }
        
        return updatedItem;
    }

}
