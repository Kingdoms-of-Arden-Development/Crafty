package net.kingdomsofarden.crafty.internals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.kingdomsofarden.crafty.internals.thirdparty.comphoenix.AttributeStorage;
import net.kingdomsofarden.crafty.util.ItemManager;

import org.bukkit.inventory.ItemStack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class ItemCache {
    private Cache<ItemStack, CraftyAttribute> cache;
    
    public ItemCache() {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .softValues()
                .weakKeys()
                .expireAfterAccess(5,TimeUnit.MINUTES)
                .build(new CacheLoader<ItemStack, CraftyAttribute>() {

                    @Override
                    public CraftyAttribute load(ItemStack is) {
                        AttributeStorage as = ItemManager.getCraftyItem(is);
                        if(as == null) {
                            return null;
                        } else {
                            return CraftyAttribute.fromString(is, as.getData());
                        }
                    }
                    
                });
    }
    
    //TODO: NOTE that this does not allow for itemstacks in which there is no data: perform ItemManager.getCraftyItem() != null check first!
    public CraftyAttribute getAttribute(ItemStack is) {
        try {
            return cache.get(is);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void invalidateItemStack(ItemStack is) {
        cache.invalidate(is);
    }

    public void cache(ItemStack item) {
        cache.apply(item);
        
    }
    
}
