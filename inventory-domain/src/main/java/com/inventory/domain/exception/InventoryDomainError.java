package com.inventory.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface InventoryDomainError extends MessageSource permits InventoryDomainError.InsufficientQuantity, InventoryDomainError.InsufficientStock, InventoryDomainError.InventoryItemNotActive, InventoryDomainError.InventoryNotFoundAtLocation, InventoryDomainError.NoAvailableInventory, InventoryDomainError.QuantityNotPositive {

    record QuantityNotPositive() implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "error.allocation.quantity_not_positive";
        }
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record NoAvailableInventory(String sku) implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "error.allocation.no_available_inventory";
        }
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InsufficientStock(int available, int requested) implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "error.allocation.insufficient_stock";
        }
        public Map<String, Object> args() {
            return Map.of("available", available, "requested", requested);
        }
    }

    record InventoryItemNotActive(String sku) implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "error.allocation.inventory_item_not_active";
        }
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InventoryNotFoundAtLocation(String sku, String locationId) implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "error.allocation.inventory_not_found_at_location";
        }
        public Map<String, Object> args() {
            return Map.of("sku", sku, "locationId", locationId);
        }
    }

    record InsufficientQuantity(int available, int requested) implements InventoryDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }
        public String code() {
            return "exception.inventory.insufficient_quantity_error";
        }
        public Map<String, Object> args() {
            return  Map.of( "available", available, "requested", requested);
        }
    }

}
