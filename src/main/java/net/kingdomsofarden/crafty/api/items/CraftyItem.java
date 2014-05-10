package net.kingdomsofarden.crafty.api.items;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;
import net.kingdomsofarden.crafty.internals.NBTUtil;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public final class CraftyItem {
    
    public final UUID itemIdentifier;
    
    private ItemStack item;
    private HashMap<UUID,Module> modules;
    private Crafty plugin;
        
    public CraftyItem(Crafty plugin, UUID id, ItemStack item) {
        this.plugin = plugin;
        this.item = item;
        this.itemIdentifier = id;
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
    
    
    public void addModule(String name) {
        UUID id = this.plugin.getModuleRegistrar().getModuleUuid(name);
        this.addModule(id);
        return;
    }
    
    public void addModule(UUID id) {
        Module m = this.plugin.getModuleRegistrar().getModule(id, this.item);
        if(m != null) {
            this.modules.put(id, m);
        }
        this.updateItem();
    }
    
    
    public void updateItem() {
        List<String> lore = plugin.getConfigurationManager().getOrderedLore(this.modules);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore); 
        StringBuilder uuidStringBuilder = new StringBuilder();
        boolean write = false; // Used for determining whether a colon delimiter needs to be prepended
        for(Module m : this.modules.values()) {
            if(write) {
                uuidStringBuilder.append(":");
            } else {
                write = true;
            }
            uuidStringBuilder.append(m.getIdentifier().toString());
            String store = m.serialize();
            if(store != null) {
                NBTUtil.writeData(m.getIdentifier(), store, this.item);
            } else {
                continue;
            }
        }
    }
    

}
