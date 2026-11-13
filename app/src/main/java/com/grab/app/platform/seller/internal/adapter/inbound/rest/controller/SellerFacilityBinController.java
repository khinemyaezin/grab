package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.FacilityBinModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
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
@RequestMapping("/api/v1/facilities")
public class SellerFacilityBinController implements SellerFacilityResource.Bins {

    private final SellerInventoryCommandService commandService;
    private final SellerInventoryQueryService queryService;
    private final FacilityBinModelAssembler modelAssembler;

    public SellerFacilityBinController(
            SellerInventoryCommandService commandService,
            SellerInventoryQueryService queryService,
            FacilityBinModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping("/zones/{zoneId}/bins")
    public ResponseEntity<EntityModel<FacilityBinResponse>> createBin(
            @PathVariable String zoneId,
            @Valid @RequestBody CreateFacilityBinRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        FacilityBinResponse response = commandService.createBin(zoneId, actorId, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/facilities/bins/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(modelAssembler.toCommandModel(response));
    }

    @GetMapping("/bins/{binId}")
    public ResponseEntity<EntityModel<FacilityBinResponse>> getBin(@PathVariable String binId) {
        return queryService.getBin(binId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/bins/{binId}/activate")
    public ResponseEntity<Void> activateBin(@PathVariable String binId) {
        commandService.activateBin(binId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bins/{binId}/deactivate")
    public ResponseEntity<Void> deactivateBin(@PathVariable String binId) {
        commandService.deactivateBin(binId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bins/{binId}")
    public ResponseEntity<Void> deleteBin(@PathVariable String binId) {
        commandService.deleteBin(binId);
        return ResponseEntity.noContent().build();
    }
}
