package com.inventory.domain.exception;

import com.grab.framework.service.MessageSource;

import java.util.Map;

public sealed interface AllocationError extends MessageSource {

    record QuantityNotPositive() implements AllocationError {
        private static final String CODE = "error.allocation.quantity_not_positive";

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record NoAvailableInventory(String sku) implements AllocationError {
        private static final String CODE = "error.allocation.no_available_inventory";

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InsufficientStock(int available, int requested) implements AllocationError {
        private static final String CODE = "error.allocation.insufficient_stock";

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("available", available, "requested", requested);
        }
    }

    record InventoryItemNotActive(String sku) implements AllocationError {
        private static final String CODE = "error.allocation.inventory_item_not_active";

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InventoryNotFoundAtLocation(String sku, String locationId) implements AllocationError {
        private static final String CODE = "error.allocation.inventory_not_found_at_location";

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku, "locationId", locationId);
        }
    }
}
