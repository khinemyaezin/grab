package com.grab.store.inventory.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface InventoryServiceError extends MessageSource permits
        InventoryServiceError.LocationNotFound,
        InventoryServiceError.LocationNotFoundByCode,
        InventoryServiceError.ZoneNotFound,
        InventoryServiceError.BinNotFound,
        InventoryServiceError.InventoryNotFound,
        InventoryServiceError.ReservationNotFound,
        InventoryServiceError.LocationAlreadyExists,
        InventoryServiceError.ZoneAlreadyExists,
        InventoryServiceError.BinAlreadyExists,
        InventoryServiceError.InventoryAlreadyExistsForSkuLocation,
        InventoryServiceError.LocationInactive,
        InventoryServiceError.ReservationInventoryMismatch,
        InventoryServiceError.LocationHasDependentInventory,
        InventoryServiceError.LocationHasDependentZones,
        InventoryServiceError.ZoneHasDependentBins,
        InventoryServiceError.ProductVariantNotFound,
        InventoryServiceError.ProductVariantDeleted,
        InventoryServiceError.AddressCountryRequired,
        InventoryServiceError.UnableToAddZone,
        InventoryServiceError.UnableToAddBin,
        InventoryServiceError.InventoryScopeForbidden {

    record LocationNotFound(String locationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.location.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("locationId", locationId);
        }
    }

    record LocationNotFoundByCode(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.location.not_found_by_code";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record ZoneNotFound(String zoneId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.zone.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("zoneId", zoneId);
        }
    }

    record BinNotFound(String binId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.bin.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("binId", binId);
        }
    }

    record InventoryNotFound(String inventoryItemId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.inventory.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("inventoryItemUuid", inventoryItemId);
        }
    }

    record ReservationNotFound(String reservationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.reservation.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reservationId", reservationId);
        }
    }

    record LocationAlreadyExists(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.location.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record ZoneAlreadyExists(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.zone.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record BinAlreadyExists(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.bin.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record InventoryAlreadyExistsForSkuLocation(String sku, String locationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.inventory.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "sku", sku,
                    "locationId", locationId
            );
        }
    }

    record LocationInactive(String locationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.service.location.inactive";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("locationId", locationId);
        }
    }

    record ReservationInventoryMismatch(String reservationId, String inventoryItemId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.service.reservation.inventory_mismatch";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "reservationId", reservationId,
                    "inventoryItemUuid", inventoryItemId
            );
        }
    }

    record LocationHasDependentInventory(String locationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.location.has_dependent_inventory";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("locationId", locationId);
        }
    }

    record LocationHasDependentZones(String locationId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.location.has_dependent_zones";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("locationId", locationId);
        }
    }

    record ZoneHasDependentBins(String zoneId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.service.zone.has_dependent_bins";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("zoneId", zoneId);
        }
    }

    record ProductVariantNotFound(String sku) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.service.product_variant.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }

    record ProductVariantDeleted(String productVariantId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "inv.service.product_variant.deleted";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("productVariantId", productVariantId);
        }
    }

    record AddressCountryRequired() implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "inv.service.location.address_country_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record UnableToAddZone(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "inv.service.zone.add_failed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record UnableToAddBin(String codeValue) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "inv.service.bin.add_failed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("code", codeValue);
        }
    }

    record InventoryScopeForbidden(String platformCode, String scopeKey, String scopeId) implements InventoryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "inv.service.scope.forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "platformCode", platformCode,
                    "scopeKey", scopeKey,
                    "scopeId", scopeId
            );
        }
    }
}
