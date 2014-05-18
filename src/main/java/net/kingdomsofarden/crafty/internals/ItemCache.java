package net.kingdomsofarden.crafty.internals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.api.CraftyItem;


public class ItemCache {
    
    public class CacheRemovalListener implements RemovalListener<CacheKey, CraftyItem> {

        @Override
        public void onRemoval(RemovalNotification<CacheKey, CraftyItem> removed) {
            removed.getValue().updateItem();
        }

    }

    private Cache<CacheKey, CraftyItem> cache;
    private Crafty plugin;
    
    public ItemCache(Crafty itemApiPlugin) {
        this.plugin = itemApiPlugin;
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(15,TimeUnit.MINUTES)
                .removalListener(new CacheRemovalListener())
                .build(new CacheLoader<CacheKey, CraftyItem>() {

                    @Override
                    public CraftyItem load(CacheKey obj) {
                        System.out.println("Debug: Cache Miss");
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
