package net.kingdomsofarden.crafty;


import java.io.IOException;
import java.util.logging.Level;

import net.kingdomsofarden.crafty.api.items.ModuleRegistrar;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;
import net.kingdomsofarden.crafty.internals.ItemManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Crafty extends JavaPlugin {
    
    private static Crafty instance;
    
    private ModuleRegistrar moduleRegistrar;
    private ConfigurationManager config;
    private ItemManager itemMan;
    
    @Override
    public void onEnable() {
        instance = this;
        
        this.moduleRegistrar = new ModuleRegistrar(this);
        try {
            this.config = new ConfigurationManager(this);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "A problem was encountered while loading the config!");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.itemMan = new ItemManager(this);
        
    }
    
    public static Crafty getInstance() {
        return instance;
    }
    
    public ModuleRegistrar getModuleRegistrar() {
        return this.moduleRegistrar;
    }

    public ConfigurationManager getConfigurationManager() {
        return this.config;
    }

    public ItemManager getItemManager() {
        return this.itemMan;
    }

}
