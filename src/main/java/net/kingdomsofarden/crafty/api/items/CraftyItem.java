package net.kingdomsofarden.crafty.api.items;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public final class CraftyItem {
    
    private ItemStack item;
    private String displayName;
    private Map<Enchantment,Integer> enchants;    
    private Deque<ItemMetaModification> metaChangeQueue; //Process all in one tick due to possible multiple changes
    private boolean processQueue; //Indicates a task to process the queue has already been scheduled
    private HashMap<UUID,Module> modules;
    
    public final UUID itemIdentifier;
    
    public CraftyItem(Crafty plugin, UUID id, ItemStack item) {
        this.item = item;
        this.itemIdentifier = id;
        ItemMeta meta = this.item.getItemMeta();
        this.displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        String moduleParse = plugin.getItemManager().getModules(item);
        if(moduleParse != null) {
            String[] moduleParsed = moduleParse.split(":");
            ModuleRegistrar registrar = plugin.getModuleRegistrar();
            ConfigurationManager config = plugin.getConfigurationManager();
            for(String idString : moduleParsed) {
                try {
                    UUID moduleId = UUID.fromString(idString);
                    //Handle migrations is necessary
                    UUID migratedId = config.getMigratedModule(moduleId);
                    while(migratedId != null) {
                        moduleId = migratedId;
                        migratedId = config.getMigratedModule(migratedId);
                    }
                    Module modToAdd = registrar.getModule(migratedId, item);
                    if(modToAdd != null) {
                        this.modules.put(migratedId, modToAdd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
    
    public HashMap<UUID,Module> getModules() {
        return this.modules;
    }
    
    public void setDisplayName(String name) {
        this.displayName = name;
        metaChangeQueue.add(new ItemMetaModification() {

            @Override
            public void modifyMeta(ItemMeta meta) {
                meta.setDisplayName(displayName);
            }
            
        });
        if(!this.processQueue) {
            Bukkit.getScheduler().runTask(Crafty.getInstance(), new ItemMetaModificationTask());
            this.processQueue = true;
        }
    }
    
    public void setLore(List<String> itemLore) {
        throw new UnsupportedOperationException("Attach an additional module to modify lore "
                + "direct modification of lore is not supported!");
    }
    
    public void setEnchantments(Map<Enchantment,Integer> itemEnchants) {
        this.enchants = itemEnchants;
        metaChangeQueue.add(new ItemMetaModification() {

            @Override
            public void modifyMeta(ItemMeta meta) {
                for(Enchantment e : enchants.keySet()) {
                    meta.addEnchant(e, enchants.get(e), true);
                }
            }
            
        });
        if(!this.processQueue) {
            Bukkit.getScheduler().runTask(Crafty.getInstance(), new ItemMetaModificationTask());
            this.processQueue = true;
        }
    }
    
    private abstract class ItemMetaModification {
        public abstract void modifyMeta(ItemMeta meta);
    }
    
    private class ItemMetaModificationTask implements Runnable {

        @Override
        public void run() {
            processQueue = false;
            ItemMeta meta = item.getItemMeta();
            for(ItemMetaModification itemMetaMod : metaChangeQueue) {
                itemMetaMod.modifyMeta(meta);
            }
            item.setItemMeta(meta);
        }
        
    }
    

}
