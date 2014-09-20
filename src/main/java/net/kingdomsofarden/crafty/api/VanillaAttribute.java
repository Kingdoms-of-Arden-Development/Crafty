package net.kingdomsofarden.crafty.api;

import com.comphenix.attribute.Attributes.AttributeType;

public enum VanillaAttribute {
    HEALTH(AttributeType.GENERIC_MAX_HEALTH),
    KNOCKBACK_RESISTANCE(AttributeType.GENERIC_KNOCKBACK_RESISTANCE),
    MOVEMENT_SPEED(AttributeType.GENERIC_MOVEMENT_SPEED);

    final AttributeType nbtType;

    VanillaAttribute(AttributeType nbtType) {
        this.nbtType = nbtType;
    }

    AttributeType getNbtType() {
        return nbtType;
    }
}