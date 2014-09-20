package net.kingdomsofarden.crafty.api;

import com.comphenix.attribute.Attributes.Operation;

public enum AttributeOperation {
    ADD_NUMBER(Operation.ADD_NUMBER),
    MULTIPLY_PERCENTAGE(Operation.MULTIPLY_PERCENTAGE),
    ADD_PERCENTAGE(Operation.ADD_PERCENTAGE);

    final Operation operation;

    AttributeOperation(Operation operation) {
        this.operation = operation;
    }

    Operation getOperation() {
        return operation;
    }
}
