package net.kingdomsofarden.crafty.internals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.api.CraftyItem;


public class ItemCache {
    
    private Cache<CacheKey, CraftyItem> cache;
    private Crafty plugin;
    
    public ItemCache(Crafty itemApiPlugin) {
        this.plugin = itemApiPlugin;
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .softValues()
                .weakKeys()
                .expireAfterAccess(15,TimeUnit.MINUTES)
                .build(new CacheLoader<CacheKey, CraftyItem>() {

                    @Override
                    public CraftyItem load(CacheKey obj) {
                        return new CraftyItem(obj, plugin); 
                    }
                });
    }
    
    public CraftyItem get(CacheKey key) throws ExecutionException {
        return this.cache.get(key);
    }
    
    public Cache<CacheKey,CraftyItem> get() {
        return this.cache;
    }
}
