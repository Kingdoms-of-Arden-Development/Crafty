package net.kingdomsofarden.crafty.internals;

import java.util.Arrays;
import java.util.UUID;

public class CraftyAttribute {

    private UUID[] interestedItems;
    private UUID version;

    public CraftyAttribute(UUID craftyVersion, UUID... id) {
        this.version = craftyVersion;
        this.interestedItems = id;
    }

    public static CraftyAttribute fromString(String parseable) {
        String[] split = parseable.split(":");
        UUID version = null;
        try {
            version = UUID.fromString(split[0]);
        } catch(IllegalArgumentException e) {
            System.out.println("Error Processing Crafty Attribute!");
            e.printStackTrace();
            return null;
        }
        UUID[] itemIds = new UUID[split.length - 1]; //Preallocate to this size to prevent repeated copying
        try {
            for(int i = 1; i < split.length; i++) {
                itemIds[i-1] = UUID.fromString(split[i]);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error Processing Crafty Attribute!");
            e.printStackTrace();
            return null;
        }
        return new CraftyAttribute(version,itemIds);
    }

    public void insert(UUID id) {
        this.interestedItems = Arrays.copyOf(this.interestedItems, this.interestedItems.length + 1);
        this.interestedItems[this.interestedItems.length - 1] = id;
    }
    
    public UUID[] getInterestedItems() {
        return this.interestedItems;
    }
    
    @Override
    public String toString() {
        StringBuilder sB = new StringBuilder();
        sB.append(version);
        sB.append(":");
        for(int i = 0; i < interestedItems.length; i++) {
            sB.append(interestedItems[i].toString());
            if(i < interestedItems.length - 1) {
                sB.append(":");
            }
        }
        return sB.toString();
    }



}
