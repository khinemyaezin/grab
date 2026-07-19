package com.grab.store.inventory.internal.exception;

import com.grab.framework.exception.DomainException;

public class InventoryServiceException extends DomainException {

    public InventoryServiceException(InventoryServiceError error) {
        super(error, defaultMessage(error));
    }

    public InventoryServiceException(InventoryServiceError error, String defaultMessage) {
        super(error, defaultMessage);
    }

    private static String defaultMessage(InventoryServiceError error) {
        return switch (error) {
            case InventoryServiceError.LocationNotFound e ->
                    "Location not found: " + e.locationId();
            case InventoryServiceError.LocationNotFoundByCode e ->
                    "Location not found for code: " + e.codeValue();
            case InventoryServiceError.ZoneNotFound e ->
                    "Zone not found: " + e.zoneId();
            case InventoryServiceError.BinNotFound e ->
                    "Bin not found: " + e.binId();
            case InventoryServiceError.InventoryNotFound e ->
                    "Inventory not found: " + e.inventoryItemId();
            case InventoryServiceError.ReservationNotFound e ->
                    "Reservation not found: " + e.reservationId();
            case InventoryServiceError.LocationAlreadyExists e ->
                    "Location already exists for code: " + e.codeValue();
            case InventoryServiceError.ZoneAlreadyExists e ->
                    "Zone already exists for code: " + e.codeValue();
            case InventoryServiceError.BinAlreadyExists e ->
                    "Bin already exists for code: " + e.codeValue();
            case InventoryServiceError.InventoryAlreadyExistsForSkuLocation ignored ->
                    "Inventory already exists for sku/location.";
            case InventoryServiceError.LocationInactive e ->
                    "Location is not active: " + e.locationId();
            case InventoryServiceError.ReservationInventoryMismatch e ->
                    "Reservation does not belong to inventory item: " + e.inventoryItemId();
            case InventoryServiceError.LocationHasDependentInventory e ->
                    "Cannot deactivate location with dependent inventory: " + e.locationId();
            case InventoryServiceError.LocationHasDependentZones e ->
                    "Cannot delete location with dependent zones: " + e.locationId();
            case InventoryServiceError.ZoneHasDependentBins e ->
                    "Cannot delete zone with dependent bins: " + e.zoneId();
            case InventoryServiceError.ProductVariantNotFound e ->
                    "Product variant not found for sku: " + e.sku();
            case InventoryServiceError.ProductVariantDeleted e ->
                    "Product variant is deleted: " + e.productVariantId();
            case InventoryServiceError.AddressCountryRequired ignored ->
                    "Address country is required";
            case InventoryServiceError.UnableToAddZone e ->
                    "Unable to add zone: " + e.codeValue();
            case InventoryServiceError.UnableToAddBin e ->
                    "Unable to add bin: " + e.codeValue();
            case InventoryServiceError.InventoryScopeForbidden e ->
                    "A valid Seller Portal inventory scope is required";
            case InventoryServiceError.TransferSameLocation e ->
                    "Cannot transfer inventory to the same location: " + e.locationId();
            case InventoryServiceError.AllocationFailed e ->
                    "Unable to allocate stock for sku=" + e.sku() + ", quantity=" + e.quantity()
                            + " (" + e.reasonCode() + ")";
        };
    }
}
