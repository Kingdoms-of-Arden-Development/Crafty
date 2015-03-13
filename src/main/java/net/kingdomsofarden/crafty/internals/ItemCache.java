package net.kingdomsofarden.crafty.internals;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.kingdomsofarden.crafty.Crafty;
import net.kingdomsofarden.crafty.api.CraftyItem;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class ItemCache {
    
    public class CacheRemovalListener implements RemovalListener<CacheKey, CraftyItem> {

        @Override
        public void onRemoval(RemovalNotification<CacheKey, CraftyItem> removed) {
            removed.getValue().updateItem();
        }

    }

    private LoadingCache<CacheKey, CraftyItem> cache;
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
                        return new CraftyItem(obj, plugin); 
                    }
                });
    }
    
    public CraftyItem get(CacheKey key) throws ExecutionException {
        if (this.cache.asMap().containsKey(key)) {
            if (this.cache.get(key).getItem() != key.getItem()) {
                this.cache.invalidate(key);
            }
        }
        return this.cache.getIfPresent(key);
        
    }
    
    public LoadingCache<CacheKey,CraftyItem> get() {
        return this.cache;
    }
}
