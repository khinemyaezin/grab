package com.grab.service.contract.message.merchandising.inventory;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Warehouse Facility Topology (Locations, Zones, Bins).
 * Replaces LocationController, ZoneController, and BinController.
 */
public interface InventoryFacilityPort extends Remote {

    FacilityLocationResult createLocation(CreateLocationMessage message) throws RemoteException;

    void activateLocation(Id locationId) throws RemoteException;

    void deactivateLocation(Id locationId) throws RemoteException;

    void deleteLocation(Id locationId) throws RemoteException;

    FacilityLocationResult findLocationById(Id locationId) throws RemoteException;

    FacilityZoneResult createZone(Id locationId, CreateZoneMessage message) throws RemoteException;

    void activateZone(Id zoneId) throws RemoteException;

    void deactivateZone(Id zoneId) throws RemoteException;

    void deleteZone(Id zoneId) throws RemoteException;

    FacilityZoneResult findZoneById(Id zoneId) throws RemoteException;

    FacilityBinResult createBin(Id zoneId, CreateBinMessage message) throws RemoteException;

    void activateBin(Id binId) throws RemoteException;

    void deactivateBin(Id binId) throws RemoteException;

    void deleteBin(Id binId) throws RemoteException;

    FacilityBinResult findBinById(Id binId) throws RemoteException;

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
    ) implements Serializable {}

    record CreateZoneMessage(
            String code,
            String name,
            String type,
            String actorId,
            String scopeKey,
            String scopeId
    ) implements Serializable {}

    record CreateBinMessage(
            String code,
            String name,
            Integer maxCapacity,
            String actorId,
            String scopeKey,
            String scopeId
    ) implements Serializable {}

    record FacilityLocationResult(
            Id id,
            Id merchantId,
            String code,
            String name,
            String type,
            boolean active
    ) implements Serializable {}

    record FacilityZoneResult(
            Id id,
            Id locationId,
            String code,
            String name,
            String type,
            boolean active
    ) implements Serializable {}

    record FacilityBinResult(
            Id id,
            Id zoneId,
            String code,
            String name,
            Integer maxCapacity,
            boolean active
    ) implements Serializable {}
}
