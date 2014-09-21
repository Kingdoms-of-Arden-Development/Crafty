package net.kingdomsofarden.crafty.plugins;

import net.kingdomsofarden.crafty.CraftyPlugin;
import net.kingdomsofarden.crafty.api.ItemManager;
import net.kingdomsofarden.crafty.api.ModuleRegistrar;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;

import java.util.logging.Logger;

public class CraftySpongePlugin implements CraftyPlugin {
    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public ItemManager getItemManager() {
        return null;
    }

    @Override
    public ModuleRegistrar getModuleRegistrar() {
        return null;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return null;
    }

}
