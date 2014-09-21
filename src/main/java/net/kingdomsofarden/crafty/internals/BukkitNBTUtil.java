package net.kingdomsofarden.crafty.internals;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.comphenix.attribute.Attributes;
import org.bukkit.inventory.ItemStack;

import com.comphenix.attribute.AttributeStorage;
import org.bukkit.inventory.meta.ItemMeta;

public class BukkitNBTUtil implements NBTUtil {

    @Override
    public CacheKey getCacheKey(Object item) {
        AttributeStorage storage = AttributeStorage.newTarget((ItemStack) item, CRAFTY_ITEM_TRACKER);
        if (storage.getData(null) != null) {
            return new CacheKey(item, UUID.fromString(storage.getData(null)));
        } else {
            UUID id = UUID.randomUUID();
            storage.setData(id.toString());
            item = storage.getTarget();
            return new CacheKey(item, id);
        }
    }
    

    @Override
    public UUID getItemTrackerId(Object item) {
        AttributeStorage storage = AttributeStorage.newTarget((ItemStack) item, CRAFTY_ITEM_TRACKER);
        if (storage.getData(null) != null) {
            return UUID.fromString(storage.getData(null));
        } else {
            return null;
        }
    }

    @Override
    public boolean hasData(Object item, UUID id) {
        AttributeStorage storage = AttributeStorage.newTarget((ItemStack) item, id);
        return storage.hasData();
    }

    @Override
    public String getData(UUID id, Object item) {
        AttributeStorage storage = AttributeStorage.newTarget((ItemStack) item, id);
        return storage.getData(null);
    }
    
    @Override
    public void writeData(UUID id, String data, Object item) {
        if (data == null || data.equals("")) {
            throw new IllegalArgumentException("Stored data is null or empty for NBT id " + id);
        }
        AttributeStorage storage = AttributeStorage.newTarget((ItemStack) item, id);
        storage.setData(data);
        if (storage.getTarget() != item) {
            throw new IllegalArgumentException("Item target changed during NBT Write - Are you sure you wrote to a Crafty Item?");
        }
    }

    @Override
    public void writeVanillaAttributes(Collection<AttributeInfo> values, Object item) {
        Attributes a = new Attributes((ItemStack) item);
        for (AttributeInfo info : values) {
            a.add(info.toAttribute());
        }
        if (a.getStack() != item) {
            throw new IllegalArgumentException("Item target changed during NBT Write - Are you sure you wrote to a Crafty Item?");
        }
    }

    @Override
    public void setLore(List<String> lore, Object obj) {
        ItemStack item = (ItemStack)obj;
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

}
