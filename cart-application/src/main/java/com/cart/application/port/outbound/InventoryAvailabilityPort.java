package com.cart.application.port.outbound;

public interface InventoryAvailabilityPort {
    Availability available(String sku, String salesChannelId);

    record Availability(int availableQty, boolean untracked) {
    }
}
