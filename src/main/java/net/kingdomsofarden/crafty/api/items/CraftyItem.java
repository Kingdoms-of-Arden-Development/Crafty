package net.kingdomsofarden.crafty.api.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.CraftyAttribute;
import net.kingdomsofarden.crafty.internals.thirdparty.comphoenix.AttributeStorage;
import net.kingdomsofarden.crafty.util.ItemManager;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public abstract class CraftyItem {
    
    private String displayName = null;
    private List<String> lore = null;
    private Map<Enchantment,Integer> enchants = null;    
    private Crafty plugin;
    
    public final UUID itemUniqueId;
    
    public CraftyItem(UUID id, Crafty plugin) {
        this.itemUniqueId = id;
        this.plugin = plugin;
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
        //TODO: Cache check
        AttributeStorage storage = ItemManager.getCraftyItem(item);
        if(storage == null) {
            return false;
        } else {
            CraftyAttribute attrib = CraftyAttribute.fromString(item,storage.getData());
            if(attrib.contains(this.itemUniqueId)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    public final ItemStack expressInterest(ItemStack item) {
        //TODO: Cache check
        AttributeStorage storage = AttributeStorage.newTarget(item,ItemManager.PluginUUID);
        
        String parseable = storage.getData();
        
        CraftyAttribute attrib = null;
        
        if(parseable != null) {
            attrib = CraftyAttribute.fromString(item, parseable);
            attrib.insert(itemUniqueId);
        } else {
            attrib = new CraftyAttribute(item, itemUniqueId);
        }
        
        storage.setData(attrib.toString());
        
        //TODO: Cache update (or clear) if item no longer matches
        
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
