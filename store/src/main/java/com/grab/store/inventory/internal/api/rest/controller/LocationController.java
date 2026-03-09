package com.grab.store.inventory.internal.api.rest.controller;

import com.inventory.domain.enums.LocationType;
import com.grab.store.inventory.internal.api.rest.dto.request.AddBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationsResponse;
import com.grab.store.inventory.internal.api.rest.service.LocationFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationFacadeService locationFacadeService;

    @PostMapping
    public ResponseEntity<EntityModel<LocationResponse>> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationFacadeService.createLocation(request, actorId));
    }

    @PatchMapping("/{locationId}")
    public ResponseEntity<EntityModel<LocationResponse>> updateLocation(
            @PathVariable("locationId") String locationId,
            @Valid @RequestBody UpdateLocationRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.updateLocation(locationId, request, actorId));
    }

    @PostMapping("/{locationId}/activate")
    public ResponseEntity<EntityModel<LocationResponse>> activateLocation(
            @PathVariable("locationId") String locationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.activateLocation(locationId, actorId));
    }

    @PostMapping("/{locationId}/deactivate")
    public ResponseEntity<EntityModel<LocationResponse>> deactivateLocation(
            @PathVariable("locationId") String locationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.deactivateLocation(locationId, actorId));
    }

    @PostMapping("/{locationId}/zones")
    public ResponseEntity<EntityModel<LocationResponse>> addZone(
            @PathVariable("locationId") String locationId,
            @Valid @RequestBody AddZoneRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.addZone(locationId, request, actorId));
    }

    @PatchMapping("/{locationId}/zones/{zoneId}")
    public ResponseEntity<EntityModel<LocationResponse>> updateZone(
            @PathVariable("locationId") String locationId,
            @PathVariable("zoneId") String zoneId,
            @Valid @RequestBody UpdateZoneRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.updateZone(locationId, zoneId, request, actorId));
    }

    @DeleteMapping("/{locationId}/zones/{zoneId}")
    public ResponseEntity<EntityModel<LocationResponse>> removeZone(
            @PathVariable("locationId") String locationId,
            @PathVariable("zoneId") String zoneId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.removeZone(locationId, zoneId, actorId));
    }

    @PostMapping("/{locationId}/zones/{zoneId}/bins")
    public ResponseEntity<EntityModel<LocationResponse>> addBin(
            @PathVariable("locationId") String locationId,
            @PathVariable("zoneId") String zoneId,
            @Valid @RequestBody AddBinRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.addBin(locationId, zoneId, request, actorId));
    }

    @PatchMapping("/{locationId}/zones/{zoneId}/bins/{binId}")
    public ResponseEntity<EntityModel<LocationResponse>> updateBin(
            @PathVariable("locationId") String locationId,
            @PathVariable("zoneId") String zoneId,
            @PathVariable("binId") String binId,
            @Valid @RequestBody UpdateBinRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.updateBin(locationId, zoneId, binId, request, actorId));
    }

    @DeleteMapping("/{locationId}/zones/{zoneId}/bins/{binId}")
    public ResponseEntity<EntityModel<LocationResponse>> removeBin(
            @PathVariable("locationId") String locationId,
            @PathVariable("zoneId") String zoneId,
            @PathVariable("binId") String binId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(locationFacadeService.removeBin(locationId, zoneId, binId, actorId));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<EntityModel<LocationResponse>> getLocation(@PathVariable("locationId") String locationId) {
        return ResponseEntity.ok(locationFacadeService.getLocation(locationId));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EntityModel<LocationResponse>> getLocationByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(locationFacadeService.getLocationByCode(code));
    }

    @GetMapping
    public ResponseEntity<EntityModel<LocationsResponse>> listLocations(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) LocationType type
    ) {
        return ResponseEntity.ok(locationFacadeService.listLocations(active, type));
    }
}
