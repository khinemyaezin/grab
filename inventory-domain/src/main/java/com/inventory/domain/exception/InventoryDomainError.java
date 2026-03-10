package com.inventory.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface InventoryDomainError extends MessageSource permits
        InventoryDomainError.QuantityNotPositive,
        InventoryDomainError.NoAvailableInventory,
        InventoryDomainError.InsufficientStock,
        InventoryDomainError.InventoryItemNotActive,
        InventoryDomainError.InventoryNotFoundAtLocation,
        InventoryDomainError.InsufficientQuantity,
        InventoryDomainError.InvalidStockMovementType,
        InventoryDomainError.NegativeQuantity,
        InventoryDomainError.InvalidOnHandQuantity,
        InventoryDomainError.InvalidReservedQuantity,
        InventoryDomainError.InvalidInTransitQuantity,
        InventoryDomainError.InvalidDamagedQuantity,
        InventoryDomainError.SubtractExceedsOnHand,
        InventoryDomainError.ReserveExceedsAvailable,
        InventoryDomainError.ReleaseExceedsReserved,
        InventoryDomainError.ReceiveExceedsInTransit,
        InventoryDomainError.DamageExceedsUndamaged,
        InventoryDomainError.ShipExceedsReserved,
        InventoryDomainError.InvalidSafetyStock,
        InventoryDomainError.InvalidReorderPoint,
        InventoryDomainError.InvalidReorderQuantity,
        InventoryDomainError.InvalidMaxStock,
        InventoryDomainError.InvalidReorderConfig {

    record QuantityNotPositive() implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.alloc.quantity_not_positive";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record NoAvailableInventory(String sku) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.alloc.no_available_inventory";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InsufficientStock(int available, int requested) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.alloc.insufficient_stock";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("available", available, "requested", requested);
        }
    }

    record InventoryItemNotActive(String sku) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.alloc.inventory_item_not_active";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record InventoryNotFoundAtLocation(String sku, String locationId) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.alloc.inventory_not_found_at_location";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku, "locationId", locationId);
        }
    }

    record InsufficientQuantity(int available, int requested) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.insufficient_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("available", available, "requested", requested);
        }
    }

    record InvalidStockMovementType(String stockMovementType) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_stock_movement_type";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("stockMovementType", stockMovementType);
        }
    }

    record NegativeQuantity(int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.negative_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("quantity", quantity);
        }
    }

    record InvalidOnHandQuantity(int onHand) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_on_hand_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("onHand", onHand);
        }
    }

    record InvalidReservedQuantity(int reserved) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_reserved_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reserved", reserved);
        }
    }

    record InvalidInTransitQuantity(int inTransit) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_in_transit_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("inTransit", inTransit);
        }
    }

    record InvalidDamagedQuantity(int damaged) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_damaged_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("damaged", damaged);
        }
    }

    record SubtractExceedsOnHand(int onHand, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.subtract_exceeds_on_hand";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("onHand", onHand, "quantity", quantity);
        }
    }

    record ReserveExceedsAvailable(int available, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.reserve_exceeds_available";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("available", available, "quantity", quantity);
        }
    }

    record ReleaseExceedsReserved(int reserved, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.release_exceeds_reserved";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reserved", reserved, "quantity", quantity);
        }
    }

    record ReceiveExceedsInTransit(int inTransit, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.receive_exceeds_in_transit";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("inTransit", inTransit, "quantity", quantity);
        }
    }

    record DamageExceedsUndamaged(int undamaged, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.damage_exceeds_undamaged";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("undamaged", undamaged, "quantity", quantity);
        }
    }

    record ShipExceedsReserved(int reserved, int quantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.ship_exceeds_reserved";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reserved", reserved, "quantity", quantity);
        }
    }

    record InvalidSafetyStock(int safetyStock) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_safety_stock";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("safetyStock", safetyStock);
        }
    }

    record InvalidReorderPoint(int reorderPoint) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_reorder_point";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reorderPoint", reorderPoint);
        }
    }

    record InvalidReorderQuantity(int reorderQuantity) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_reorder_quantity";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reorderQuantity", reorderQuantity);
        }
    }

    record InvalidMaxStock(int maxStock) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_max_stock";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("maxStock", maxStock);
        }
    }

    record InvalidReorderConfig(int safetyStock, int reorderPoint) implements InventoryDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.domain.invalid_reorder_config";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "safetyStock", safetyStock,
                    "reorderPoint", reorderPoint
            );
        }
    }
}
