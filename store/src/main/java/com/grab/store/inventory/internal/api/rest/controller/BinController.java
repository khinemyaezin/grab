package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.BinModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.BinCommandService;
import com.grab.store.inventory.internal.api.rest.service.BinQueryService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.grab.store.shared.security.SecurityPrincipal;

@RestController
@RequestMapping("/api/v1/inventory/bins")
@RequiredArgsConstructor
public class BinController {

    private final BinCommandService binCommandService;
    private final BinQueryService binQueryService;
    private final BinModelAssembler binModelAssembler;
    private final AuthenticatedInventoryScopeResolver scopeResolver;

    @PostMapping("")
    public ResponseEntity<EntityModel<BinResponse>> createBin(
            @Valid @RequestBody CreateBinRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorId = scopeResolver.resolveOwnerMerchantId(principal);
        BinResponse response = binCommandService.createBin(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}")
    public ResponseEntity<EntityModel<BinResponse>> updateBin(
            @PathVariable String binId,
            @Valid @RequestBody UpdateBinRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorId = scopeResolver.resolveOwnerMerchantId(principal);
        BinResponse response = binCommandService.updateBin(binId, request, actorId);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}/activate")
    public ResponseEntity<EntityModel<BinResponse>> activateBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorId = scopeResolver.resolveOwnerMerchantId(principal);
        BinResponse response = binCommandService.activateBin(binId, actorId);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}/deactivate")
    public ResponseEntity<EntityModel<BinResponse>> deactivateBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorId = scopeResolver.resolveOwnerMerchantId(principal);
        BinResponse response = binCommandService.deactivateBin(binId, actorId);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @DeleteMapping("/{binId}")
    public ResponseEntity<Void> deleteBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorId = scopeResolver.resolveOwnerMerchantId(principal);
        binCommandService.deleteBin(binId, actorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{binId}")
    public ResponseEntity<EntityModel<BinResponse>> getBinById(
            @PathVariable String binId
    ) {
        BinResponse response = binQueryService.getBin(binId);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @GetMapping("/zones/{zoneId}")
    public ResponseEntity<PagedModel<EntityModel<BinResponse>>> listBins(
            @PathVariable String zoneId,
            @RequestParam(value = "active", required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<BinResponse> pagedResourcesAssembler
    ) {
        Page<BinResponse> responses = binQueryService.listBins(zoneId, active, pageable);
        PagedModel<EntityModel<BinResponse>> pagedModel = pagedResourcesAssembler.toModel(responses, binModelAssembler);
        return ResponseEntity.ok(pagedModel);
    }
}
