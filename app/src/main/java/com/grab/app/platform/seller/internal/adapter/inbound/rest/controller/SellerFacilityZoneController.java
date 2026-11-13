package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.FacilityZoneModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor

public class SellerFacilityZoneController {

    private final SellerInventoryCommandService commandService;
    private final SellerInventoryQueryService queryService;
    private final FacilityZoneModelAssembler modelAssembler;

    @PostMapping("/locations/{locationId}/zones")
    public ResponseEntity<EntityModel<FacilityZoneResponse>> createZone(
            @PathVariable String locationId,
            @Valid @RequestBody CreateFacilityZoneRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        FacilityZoneResponse response = commandService.createZone(locationId, principal.getPlatformUserId(), request);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/facilities/zones/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(modelAssembler.toCommandModel(response));
    }

    @GetMapping("/zones/{zoneId}")
    public ResponseEntity<EntityModel<FacilityZoneResponse>> getZone(@PathVariable String zoneId) {
        return queryService.getZone(zoneId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/zones/{zoneId}/activate")
    public ResponseEntity<Void> activateZone(@PathVariable String zoneId) {
        commandService.activateZone(zoneId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/zones/{zoneId}/deactivate")
    public ResponseEntity<Void> deactivateZone(@PathVariable String zoneId) {
        commandService.deactivateZone(zoneId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/zones/{zoneId}")
    public ResponseEntity<Void> deleteZone(@PathVariable String zoneId) {
        commandService.deleteZone(zoneId);
        return ResponseEntity.noContent().build();
    }
}
