package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.FacilityLocationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import com.grab.app.platform.seller.port.rest.SellerFacilityResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/facilities/locations")
public class SellerFacilityLocationController implements SellerFacilityResource {

    private final SellerInventoryCommandService commandService;
    private final SellerInventoryQueryService queryService;
    private final FacilityLocationModelAssembler modelAssembler;

    public SellerFacilityLocationController(
            SellerInventoryCommandService commandService,
            SellerInventoryQueryService queryService,
            FacilityLocationModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<FacilityLocationResponse>> createLocation(
            @Valid @RequestBody CreateFacilityLocationRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        FacilityLocationResponse response = commandService.createLocation(merchantId, actorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<EntityModel<FacilityLocationResponse>> getLocation(@PathVariable String locationId) {
        return queryService.getLocation(locationId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{locationId}/activate")
    public ResponseEntity<Void> activateLocation(@PathVariable String locationId) {
        commandService.activateLocation(locationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{locationId}/deactivate")
    public ResponseEntity<Void> deactivateLocation(@PathVariable String locationId) {
        commandService.deactivateLocation(locationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String locationId) {
        commandService.deleteLocation(locationId);
        return ResponseEntity.noContent().build();
    }
}
