package com.inventory.domain.enums;

public enum AllocationError {
    QUANTITY_NOT_POSITIVE("Quantity must be positive"),
    NO_AVAILABLE_INVENTORY("No available inventory found for SKU: %s"),
    INSUFFICIENT_STOCK("Insufficient stock. Available: %d, Requested: %d"),
    INVENTORY_ITEM_NOT_ACTIVE("Inventory item is not active"),
    INVENTORY_NOT_FOUND_AT_LOCATION("No inventory found for SKU %s at location %s"),
    PARTIAL_ALLOCATION("Partial allocation: requested %d, allocated %d");

    private final String messageTemplate;

    AllocationError(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getMessage() {
        return messageTemplate;
    }

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }
}
