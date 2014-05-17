package net.kingdomsofarden.crafty.internals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.api.items.Module;
import net.kingdomsofarden.crafty.api.items.ModuleRegistrar;

public class ConfigurationManager {
    
    private Crafty plugin;
    private ModuleRegistrar registrar;
    private FileConfiguration config;
    private LinkedHashSet<UUID> orderedModulesByUUID;
    private Map<UUID,UUID> migrationMap;
    
    private static final String CONFIGKEY_MODULE_ORDER = "modules.order";
    private static final String CONFIGKEY_MODULE_MIGRATION = "modules.migration";
    
    public ConfigurationManager(Crafty plugin) throws IOException {
        this.orderedModulesByUUID = new LinkedHashSet<UUID>();
        this.plugin = plugin;
        this.registrar = plugin.getModuleRegistrar();
        this.loadConfig();
        for(String string : this.config.getStringList(CONFIGKEY_MODULE_ORDER)) {
            UUID map = registrar.getModuleUuid(string);
            if(map != null) {
                this.orderedModulesByUUID.add(map);
            }
        }
        this.migrationMap = new HashMap<UUID,UUID>();
        for(String string : this.config.getStringList(CONFIGKEY_MODULE_MIGRATION)) {
            String[] parsed = string.split(">");
            try {
                migrationMap.put(UUID.fromString(parsed[0]), UUID.fromString(parsed[1]));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in migration config for " + string);
                e.printStackTrace();
            }
        }
    }
    
    private void loadConfig() {
        this.config = plugin.getConfig();
    }
    
    public List<String> getOrderedLore(Map<UUID,Module> modules) {
        List<String> lore = new LinkedList<String>();
//        for(UUID id : this.orderedModulesByUUID) {
//            if(modules.containsKey(id)) {
//                List<String> append = modules.get(id).getLoreSection();
//                if(append != null) {
//                    lore.addAll(append);
//                }
//            }
//        }
        for(Module m : modules.values()) {
            List<String> append = m.getLoreSection();
            if(append != null) {
                for(String string : append) {
                    lore.add(string);
                }
            }
        }
        return lore;
    }
    
    public void registerModule(UUID id, String name) {
        if(orderedModulesByUUID.contains(id)) {
            return;
        } else {
            List<String> preexisting = config.getStringList(CONFIGKEY_MODULE_ORDER);
            preexisting.add(name);
            this.config.set(CONFIGKEY_MODULE_ORDER, preexisting);
        }
    }
    
    public boolean hasMigration(UUID id) {
        return this.migrationMap.containsKey(id);
    }
    
    public UUID getMigratedModule(UUID id) {
        return this.migrationMap.get(id);
    }
    
    
}
