package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Warehouse Facility Topology (Locations, Zones, Bins).
 * Replaces LocationController, ZoneController, and BinController.
 */
public interface InventoryFacilityPort {

    FacilityLocationResult createLocation(CreateLocationMessage message);

    void activateLocation(Id locationId);

    void deactivateLocation(Id locationId);

    void deleteLocation(Id locationId);

    Optional<FacilityLocationResult> findLocationById(Id locationId);

    FacilityZoneResult createZone(Id locationId, CreateZoneMessage message);

    void activateZone(Id zoneId);

    void deactivateZone(Id zoneId);

    void deleteZone(Id zoneId);

    Optional<FacilityZoneResult> findZoneById(Id zoneId);

    FacilityBinResult createBin(Id zoneId, CreateBinMessage message);

    void activateBin(Id binId);

    void deactivateBin(Id binId);

    void deleteBin(Id binId);

    Optional<FacilityBinResult> findBinById(Id binId);

    record CreateLocationMessage(
            String merchantId,
            String code,
            String name,
            String type,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            Id createdBy
    ) {}

    record CreateZoneMessage(
            String code,
            String name,
            String type,
            String actorId,
            String scopeKey,
            String scopeId
    ) {}

    record CreateBinMessage(
            String code,
            String name,
            Integer maxCapacity,
            String actorId,
            String scopeKey,
            String scopeId
    ) {}

    record FacilityLocationResult(
            Id id,
            Id merchantId,
            String code,
            String name,
            String type,
            boolean active
    ) {}

    record FacilityZoneResult(
            Id id,
            Id locationId,
            String code,
            String name,
            String type,
            boolean active
    ) {}

    record FacilityBinResult(
            Id id,
            Id zoneId,
            String code,
            String name,
            Integer maxCapacity,
            boolean active
    ) {}
}
