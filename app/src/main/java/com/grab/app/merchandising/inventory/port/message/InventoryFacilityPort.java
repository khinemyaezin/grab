package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Warehouse Facility Topology (Locations, Zones, Bins).
 * Replaces LocationController, ZoneController, and BinController.
 */
public interface InventoryFacilityPort {

    LocationResult createLocation(CreateLocationMessage message);

    LocationResult updateLocation(Id locationId, UpdateLocationMessage message);

    void activateLocation(Id locationId);

    void deactivateLocation(Id locationId);

    void deleteLocation(Id locationId);

    Optional<LocationResult> findLocationById(Id locationId);

    Optional<LocationResult> findLocationByCode(String code);

    List<LocationResult> searchLocations(String query, int page, int size);

    ZoneResult createZone(Id locationId, CreateZoneMessage message);

    ZoneResult updateZone(Id zoneId, UpdateZoneMessage message);

    void activateZone(Id zoneId);

    void deactivateZone(Id zoneId);

    void deleteZone(Id zoneId);

    Optional<ZoneResult> findZoneById(Id zoneId);

    List<ZoneResult> listZonesByLocation(Id locationId);

    BinResult createBin(Id zoneId, CreateBinMessage message);

    BinResult updateBin(Id binId, UpdateBinMessage message);

    void activateBin(Id binId);

    void deactivateBin(Id binId);

    void deleteBin(Id binId);

    Optional<BinResult> findBinById(Id binId);

    List<BinResult> listBinsByZone(Id zoneId);

    record CreateLocationMessage(
            Id merchantId,
            String code,
            String name,
            String addressLine1,
            String city,
            String countryCode
    ) {}

    record UpdateLocationMessage(
            String name,
            String addressLine1,
            String city
    ) {}

    record LocationResult(
            Id id,
            Id merchantId,
            String code,
            String name,
            String status,
            Instant createdAt
    ) {}

    record CreateZoneMessage(
            String code,
            String name,
            String zoneType
    ) {}

    record UpdateZoneMessage(
            String name,
            String zoneType
    ) {}

    record ZoneResult(
            Id id,
            Id locationId,
            String code,
            String name,
            String zoneType,
            String status
    ) {}

    record CreateBinMessage(
            String code,
            String aisle,
            String shelf,
            int maxCapacity
    ) {}

    record UpdateBinMessage(
            String aisle,
            String shelf,
            int maxCapacity
    ) {}

    record BinResult(
            Id id,
            Id zoneId,
            String code,
            String aisle,
            String shelf,
            int maxCapacity,
            String status
    ) {}
}

