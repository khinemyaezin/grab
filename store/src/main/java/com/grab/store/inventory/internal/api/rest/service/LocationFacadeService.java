package com.grab.store.inventory.internal.api.rest.service;

import com.inventory.domain.enums.LocationType;
import com.grab.store.inventory.internal.api.rest.dto.request.AddBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationFacadeService {

    private final LocationCommandService locationCommandService;
    private final LocationQueryService locationQueryService;

    public EntityModel<LocationResponse> createLocation(CreateLocationRequest request, String actorId) {
        return locationCommandService.createLocation(request, actorId);
    }

    public EntityModel<LocationResponse> updateLocation(String locationId, UpdateLocationRequest request, String actorId) {
        return locationCommandService.updateLocation(locationId, request, actorId);
    }

    public EntityModel<LocationResponse> activateLocation(String locationId, String actorId) {
        return locationCommandService.activateLocation(locationId, actorId);
    }

    public EntityModel<LocationResponse> deactivateLocation(String locationId, String actorId) {
        return locationCommandService.deactivateLocation(locationId, actorId);
    }

    public EntityModel<LocationResponse> addZone(String locationId, AddZoneRequest request, String actorId) {
        return locationCommandService.addZone(locationId, request, actorId);
    }

    public EntityModel<LocationResponse> updateZone(String locationId, String zoneId, UpdateZoneRequest request, String actorId) {
        return locationCommandService.updateZone(locationId, zoneId, request, actorId);
    }

    public EntityModel<LocationResponse> removeZone(String locationId, String zoneId, String actorId) {
        return locationCommandService.removeZone(locationId, zoneId, actorId);
    }

    public EntityModel<LocationResponse> addBin(String locationId, String zoneId, AddBinRequest request, String actorId) {
        return locationCommandService.addBin(locationId, zoneId, request, actorId);
    }

    public EntityModel<LocationResponse> updateBin(
            String locationId,
            String zoneId,
            String binId,
            UpdateBinRequest request,
            String actorId
    ) {
        return locationCommandService.updateBin(locationId, zoneId, binId, request, actorId);
    }

    public EntityModel<LocationResponse> removeBin(String locationId, String zoneId, String binId, String actorId) {
        return locationCommandService.removeBin(locationId, zoneId, binId, actorId);
    }

    public EntityModel<LocationResponse> getLocation(String locationId) {
        return locationQueryService.getLocation(locationId);
    }

    public EntityModel<LocationResponse> getLocationByCode(String code) {
        return locationQueryService.getLocationByCode(code);
    }

    public EntityModel<LocationsResponse> listLocations(Boolean active, LocationType type) {
        return locationQueryService.listLocations(active, type);
    }
}
