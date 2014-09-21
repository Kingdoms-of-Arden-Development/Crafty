package net.kingdomsofarden.crafty.plugins;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;

import net.kingdomsofarden.crafty.CraftyPlugin;
import net.kingdomsofarden.crafty.api.ItemManager;
import net.kingdomsofarden.crafty.api.ModuleRegistrar;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;

import org.bukkit.plugin.java.JavaPlugin;

public class CraftyBukkitPlugin extends JavaPlugin implements CraftyPlugin {

    private ModuleRegistrar moduleRegistrar;
    private ConfigurationManager config;
    private ItemManager itemMan;

    @Override
    public void onLoad() {
        this.moduleRegistrar = new ModuleRegistrar(this);
    }

    @Override
    public void onEnable() {
        try { // Module Registration Lock
            Field lock = ModuleRegistrar.class.getDeclaredField("registerLock");
            lock.setAccessible(true);
            lock.set(this.moduleRegistrar, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
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
