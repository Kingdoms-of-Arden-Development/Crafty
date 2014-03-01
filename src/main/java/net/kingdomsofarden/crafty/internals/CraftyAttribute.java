package net.kingdomsofarden.crafty.internals;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class CraftyAttribute {

    private Set<UUID> interestedItems;
    private ItemStack item;

    public CraftyAttribute(ItemStack item, UUID... id) {
        this.item = item;
        this.interestedItems = new HashSet<UUID>();
        for(int i = 0; i < id.length; i++) {
            this.interestedItems.add(id[i]);
        }
    }

    public static CraftyAttribute fromString(ItemStack item, String data) {
        String[] split = data.split(":");
        UUID[] itemIds = new UUID[split.length - 1]; //Preallocate to this size to prevent repeated copying
        try {
            for(int i = 0; i < split.length; i++) {
                itemIds[i] = UUID.fromString(split[i]);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error Processing Crafty Attribute!");
            e.printStackTrace();
            return null;
        }
        return new CraftyAttribute(item, itemIds);
    }
    
    public boolean contains(UUID id) {
        return this.interestedItems.contains(id);
    }

    public void insert(UUID id) {
        this.interestedItems.add(id);
    }
    
    public Set<UUID> getInterestedItems() {
        return this.interestedItems;
    }
    
    @Override
    public String toString() {
        StringBuilder sB = new StringBuilder();
        for(UUID id : this.interestedItems) {
            sB.append(":" + id.toString() + ":");
            
        }
        return sB.toString();
    }

    public ItemStack getItem() {
        return item;
    }

}
