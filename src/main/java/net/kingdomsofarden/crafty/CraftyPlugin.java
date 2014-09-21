package net.kingdomsofarden.crafty;


import net.kingdomsofarden.crafty.api.ItemManager;
import net.kingdomsofarden.crafty.api.ModuleRegistrar;
import net.kingdomsofarden.crafty.internals.ConfigurationManager;

import java.util.logging.Logger;

public interface CraftyPlugin {
    Logger getLogger();

    ItemManager getItemManager();

    ModuleRegistrar getModuleRegistrar();

    ConfigurationManager getConfigurationManager();

}
