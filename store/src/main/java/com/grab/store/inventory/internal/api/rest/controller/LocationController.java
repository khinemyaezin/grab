package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.LocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.service.LocationCommandService;
import com.grab.store.inventory.internal.api.rest.service.LocationQueryService;
import com.inventory.domain.enums.LocationType;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/inventory/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationCommandService locationCommandService;
    private final LocationQueryService locationQueryService;
    private final LocationModelAssembler locationModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<LocationResponse>> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            @RequestHeader(value = "X-Actor-Id") String sellerId
    ) {
        LocationResponse response = locationCommandService.createLocation(request, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationModelAssembler.toModel(response));
    }

    @PatchMapping("/{locationId}")
    public ResponseEntity<EntityModel<LocationResponse>> updateLocation(
            @PathVariable String locationId,
            @Valid @RequestBody UpdateLocationRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String sellerId
    ) {
        LocationResponse response =  locationCommandService.updateLocation(locationId, request, sellerId);
        return ResponseEntity.ok(locationModelAssembler.toModel(response));
    }

    @PatchMapping("/{locationId}/activate")
    public ResponseEntity<EntityModel<LocationResponse>> activateLocation(
            @PathVariable String locationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String sellerId
    ) {
        LocationResponse response = locationCommandService.activateLocation(locationId, sellerId);
        return ResponseEntity.ok(locationModelAssembler.toModel(response));
    }

    @PatchMapping("/{locationId}/deactivate")
    public ResponseEntity<EntityModel<LocationResponse>> deactivateLocation(
            @PathVariable String locationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String sellerId
    ) {
        LocationResponse response = locationCommandService.deactivateLocation(locationId, sellerId);
        return ResponseEntity.ok(locationModelAssembler.toModel(response));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable String locationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String sellerId
    ) {
        locationCommandService.deleteLocation(locationId, sellerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<EntityModel<LocationResponse>> getLocation(
            @PathVariable String locationId) {
        LocationResponse response = locationQueryService.getLocation(locationId);
        return ResponseEntity.ok(locationModelAssembler.toModel(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EntityModel<LocationResponse>> getLocationByCode(@PathVariable String code) {
        LocationResponse response = locationQueryService.getLocationByCode(code);
        return ResponseEntity.ok(locationModelAssembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<LocationResponse>>> listLocations(
            @RequestHeader(value = "X-Actor-Id") String sellerId,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "type", required = false) LocationType type,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<LocationResponse> pagedResourcesAssembler
    ) {
        Page<LocationResponse> response = locationQueryService.listLocations(sellerId, active, type, pageable);
        PagedModel<EntityModel<LocationResponse>> pageModel = pagedResourcesAssembler.toModel(response, locationModelAssembler);
        pageModel.add(linkTo(methodOn(LocationController.class)
                .createLocation(null, null))
                .withRel("create-location"));

        return ResponseEntity.ok(pageModel);
    }
}
