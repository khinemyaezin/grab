package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.ZoneModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.service.ZoneCommandService;
import com.grab.store.inventory.internal.api.rest.service.ZoneQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/v1/inventory/zones")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneCommandService zoneCommandService;
    private final ZoneQueryService zoneQueryService;
    private final ZoneModelAssembler zoneModelAssembler;

    @PostMapping("/locations/{locationId}")
    public ResponseEntity<EntityModel<ZoneResponse>> createZone(
            @PathVariable String locationId,
            @Valid @RequestBody CreateZoneRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        ZoneResponse response = zoneCommandService.createZone(locationId, request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(zoneModelAssembler.toModel(response));
    }

    @PatchMapping("/{zoneId}")
    public ResponseEntity<EntityModel<ZoneResponse>> updateZone(
            @PathVariable String zoneId,
            @Valid @RequestBody UpdateZoneRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        ZoneResponse response = zoneCommandService.updateZone(zoneId, request, actorId);
        return ResponseEntity.ok(zoneModelAssembler.toModel(response));
    }

    @PatchMapping("/{zoneId}/activate")
    public ResponseEntity<EntityModel<ZoneResponse>> activateZone(
            @PathVariable String zoneId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        ZoneResponse response = zoneCommandService.activateZone(zoneId, actorId);
        return ResponseEntity.ok(zoneModelAssembler.toModel(response));
    }

    @PatchMapping("/{zoneId}/deactivate")
    public ResponseEntity<EntityModel<ZoneResponse>> deactivateZone(
            @PathVariable String zoneId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        ZoneResponse response = zoneCommandService.deactivateZone(zoneId, actorId);
        return ResponseEntity.ok(zoneModelAssembler.toModel(response));
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<PagedModel<EntityModel<ZoneResponse>>> listZones(
            @PathVariable String locationId,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<ZoneResponse> pagedResourcesAssembler
    ) {
        Page<ZoneResponse> response = zoneQueryService.listZones(locationId, pageable);
        PagedModel<EntityModel<ZoneResponse>> pageModel = pagedResourcesAssembler.toModel(response, zoneModelAssembler);

        pageModel.add(linkTo(methodOn(ZoneController.class)
                .createZone(locationId, null, null))
                .withRel("create-zone"));

        return ResponseEntity.ok(pageModel);
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<EntityModel<ZoneResponse>> getZoneById(
            @PathVariable String zoneId
    ) {
        ZoneResponse response = zoneQueryService.getZone(zoneId);
        return ResponseEntity.ok(zoneModelAssembler.toModel(response));
    }
}
