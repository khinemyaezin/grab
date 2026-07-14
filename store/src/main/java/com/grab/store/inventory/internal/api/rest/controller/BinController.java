package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.BinModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.service.*;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        BinResponse response = binCommandService.createBin(request, access);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}")
    public ResponseEntity<EntityModel<BinResponse>> updateBin(
            @PathVariable String binId,
            @Valid @RequestBody UpdateBinRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        BinResponse response = binCommandService.updateBin(binId, request, access);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}/activate")
    public ResponseEntity<EntityModel<BinResponse>> activateBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        BinResponse response = binCommandService.activateBin(binId, access);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @PatchMapping("/{binId}/deactivate")
    public ResponseEntity<EntityModel<BinResponse>> deactivateBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        BinResponse response = binCommandService.deactivateBin(binId, access);
        return ResponseEntity.ok(binModelAssembler.toModel(response));
    }

    @DeleteMapping("/{binId}")
    public ResponseEntity<Void> deleteBin(
            @PathVariable String binId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        binCommandService.deleteBin(binId, access);
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

    @PostMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<BinResponse>>> searchBins(
            @Valid @RequestBody SearchBinRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<BinResponse> pagedResourcesAssembler
    ) {
        String merchantId = scopeResolver.resolveOwnerMerchantId(principal);
        Page<BinResponse> response = binQueryService.searchBins(merchantId, request, pageable);
        PagedModel<EntityModel<BinResponse>> pageModel = pagedResourcesAssembler.toModel(response, binModelAssembler);

        pageModel.add(linkTo(methodOn(BinController.class)
                .createBin(null, null))
                .withRel("create-bin"));

        if (request.zoneId() != null && !request.zoneId().isBlank()) {
            pageModel.add(linkTo(methodOn(BinController.class)
                    .searchBins(null, null, null, null))
                    .withRel("search-bins"));

            pageModel.add(linkTo(methodOn(BinController.class)
                    .listBins(request.zoneId(), null, null, null))
                    .withRel("list-bins"));
        }

        return ResponseEntity.ok(pageModel);
    }
}
