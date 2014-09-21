package net.kingdomsofarden.crafty.internals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import net.kingdomsofarden.crafty.CraftyPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.api.Module;
import net.kingdomsofarden.crafty.api.ModuleRegistrar;

public class ConfigurationManager {
    
    private CraftyPlugin plugin;
    private ModuleRegistrar registrar;
    private FileConfiguration config;
    private HashMap<UUID,Integer> orderedModulesByUUID;
    private Map<UUID,UUID> migrationMap;
    private boolean orderMappingEmpty;
    
    private static final String CONFIGKEY_MODULE_ORDER = "modules.order";
    private static final String CONFIGKEY_MODULE_MIGRATION = "modules.migration";
    
    public ConfigurationManager(CraftyPlugin plugin) throws IOException {
        this.plugin = plugin;
        this.registrar = plugin.getModuleRegistrar();
        this.loadConfig();
        this.reloadConfigValues();
        for (Map.Entry<UUID,String> entry : this.registrar.getRegisteredModules().entrySet()) {
            this.registerModule(entry.getKey(), entry.getValue());
        }
        this.reloadConfigValues();
    }
    
    private void loadConfig() {
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }
    
    private void reloadConfigValues() {
        this.orderedModulesByUUID = new HashMap<UUID,Integer>();
        int weight = 0;
        for (String string : this.config.getStringList(CONFIGKEY_MODULE_ORDER)) {
            weight++;
            UUID map = registrar.getModuleUuid(string);
            if (map != null) {
                this.orderedModulesByUUID.put(map, weight);
            }
        }
        this.orderMappingEmpty = orderedModulesByUUID.size() == 0;
        this.migrationMap = new HashMap<UUID,UUID>();
        for (String string : this.config.getStringList(CONFIGKEY_MODULE_MIGRATION)) {
            String[] parsed = string.split(">");
            try {
                migrationMap.put(UUID.fromString(parsed[0]), UUID.fromString(parsed[1]));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in migration config for " + string);
                e.printStackTrace();
            }
        }
    }
    
    private void saveConfig() {
        this.plugin.saveConfig();
    }
    
    public List<String> getOrderedLore(Map<UUID, Module> modules) {
        int sortSize = modules.keySet().size();
        if (sortSize < 0 || orderMappingEmpty) {
            return null;
        }
        TreeSet<WeightedModule> sortedModules = new TreeSet<WeightedModule>();
        for (UUID id : modules.keySet()) {
            if (orderedModulesByUUID.containsKey(id)) {
                sortedModules.add(new WeightedModule(orderedModulesByUUID.get(id), modules.get(id)));
            }
        }
        List<String> lore = new LinkedList<String>();
        WeightedModule m = sortedModules.pollFirst();
        while (m != null) {
            List<String> add = m.getModule().getLoreSection();
            if (add != null) {
                lore.addAll(m.getModule().getLoreSection());
            }
            m = sortedModules.pollFirst();
        }
        return lore;
    }
    
    private void registerModule(UUID id, String name) {
        if (!orderedModulesByUUID.containsKey(id)) {
            List<String> preexisting = config.getStringList(CONFIGKEY_MODULE_ORDER);
            preexisting.add(name);
            this.config.set(CONFIGKEY_MODULE_ORDER, preexisting);
            this.saveConfig();
        }
    }
    
    public boolean hasMigration(UUID id) {
        return this.migrationMap.containsKey(id);
    }
    
    public UUID getMigratedModule(UUID id) {
        return this.migrationMap.get(id);
    }
    
    
    private class WeightedModule implements Comparable<WeightedModule> {
        
        private int weight;
        private Module m;
        
        public WeightedModule(int weight, Module m) {
            this.weight = weight;
            this.m = m;
        }
        
        public Module getModule() {
            return this.m;
        }
        
        @Override
        public int compareTo(WeightedModule o) {
            if (o == null) {
                throw new NullPointerException();
            }
            return this.weight - o.weight;
        }
        
    }
    
}
