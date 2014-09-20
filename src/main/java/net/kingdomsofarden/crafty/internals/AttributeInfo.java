package net.kingdomsofarden.crafty.internals;

import com.comphenix.attribute.Attributes.*;

import java.util.UUID;

public class AttributeInfo {
    private final UUID identifier;
    private final String name;
    private final AttributeType type;
    private final Operation operation;
    private final double amount;

    public AttributeInfo(UUID identifier, String name, AttributeType nbtType, Operation operation, double amount) {
        this.identifier = identifier;
        this.name = name;
        this.type = nbtType;
        this.operation = operation;
        this.amount = amount;
    }

    public Attribute toAttribute() {
        return Attribute.newBuilder().uuid(this.identifier).name(this.name).type(this.type)
                .operation(this.operation).amount(this.amount).build();
    }
}
