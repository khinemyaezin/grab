package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.catalog.api.CatalogApiLinks;
import com.grab.store.inventory.internal.api.rest.assembler.CheckInventoryExistenceModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryMovementModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryReservationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AnnounceInTransitRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CheckInventoryExistenceRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.MarkDamagedRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveInTransitRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReturnToVendorRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.TransferInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateReorderConfigRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.WriteOffStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.*;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import com.grab.store.inventory.internal.api.rest.service.InventoryCommandService;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
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
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryCommandService inventoryCommandService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryMovementModelAssembler inventoryMovementModelAssembler;
    private final InventoryModelAssembler inventoryModelAssembler;
    private final InventoryReservationModelAssembler inventoryReservationModelAssembler;
    private final CheckInventoryExistenceModelAssembler checkInventoryExistenceModelAssembler;
    private final AuthenticatedInventoryScopeResolver scopeResolver;

    @PostMapping
    public ResponseEntity<EntityModel<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = scopeResolver.resolveOwnerMerchantId(principal);
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.createInventory(request, merchantId, access);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<InventoryResponse>>> searchInventoryItems(
            @Valid @RequestBody SearchInventoryRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<InventoryResponse> pagedResourcesAssembler
    ) {
        String merchantId = scopeResolver.resolveOwnerMerchantId(principal);
        Page<InventoryResponse> response = inventoryQueryService.searchInventoryItems(merchantId, request, pageable);
        PagedModel<EntityModel<InventoryResponse>> pageModel = pagedResourcesAssembler.toModel(response, inventoryModelAssembler);
        pageModel.add(linkTo(methodOn(InventoryController.class)
                .searchInventoryItems(null, null, null, null))
                .withRel("search-inventory-items"));
        pageModel.add(linkTo(methodOn(InventoryController.class)
                .createInventory(null, null))
                .withRel("create-inventory-item"));
        pageModel.add(linkTo(methodOn(InventoryController.class)
                .checkExistence(null, null))
                .withRel("check-inventory-items-existence"));
        pageModel.add(CatalogApiLinks.searchProductVariants());
        return ResponseEntity.ok(pageModel);
    }

    @PostMapping("/existence")
    public ResponseEntity<EntityModel<CheckInventoryExistenceResponse>> checkExistence(
            @Valid @RequestBody CheckInventoryExistenceRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = scopeResolver.resolveOwnerMerchantId(principal);
        CheckInventoryExistenceResponse response = inventoryQueryService.checkExistence(merchantId, request);
        return ResponseEntity.ok(checkInventoryExistenceModelAssembler.toModel(response));
    }

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<EntityModel<InventoryResponse>> getInventory(
            @PathVariable String inventoryItemId) {
        InventoryResponse response = inventoryQueryService.getInventory(inventoryItemId);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/receive")
    public ResponseEntity<EntityModel<InventoryResponse>> receiveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReceiveStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.receiveStock(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/reserve")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> reserveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReserveStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryReservationResponse response = inventoryCommandService.reserveStock(
                inventoryItemId, request, idempotencyKey, access);
        return ResponseEntity.ok(inventoryReservationModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/release")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> releaseReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryReservationResponse response = inventoryCommandService.releaseReservation(inventoryItemId, reservationId, access);
        return ResponseEntity.ok(
                inventoryReservationModelAssembler.toModel(response)
        );
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/ship")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> shipReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryReservationResponse response = inventoryCommandService.shipReservation(inventoryItemId, reservationId, access);
        return ResponseEntity.ok( inventoryReservationModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/adjust")
    public ResponseEntity<EntityModel<InventoryResponse>> adjustStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody AdjustStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.adjustStock(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/damage")
    public ResponseEntity<EntityModel<InventoryResponse>> markDamaged(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody MarkDamagedRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.markDamaged(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/write-off")
    public ResponseEntity<EntityModel<InventoryResponse>> writeOff(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody WriteOffStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.writeOff(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/return-to-vendor")
    public ResponseEntity<EntityModel<InventoryResponse>> returnToVendor(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReturnToVendorRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.returnToVendor(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/suspend")
    public ResponseEntity<EntityModel<InventoryResponse>> suspend(
            @PathVariable String inventoryItemId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.suspend(inventoryItemId, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/activate")
    public ResponseEntity<EntityModel<InventoryResponse>> activate(
            @PathVariable String inventoryItemId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.activate(inventoryItemId, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/discontinue")
    public ResponseEntity<EntityModel<InventoryResponse>> discontinue(
            @PathVariable String inventoryItemId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.discontinue(inventoryItemId, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/transfer")
    public ResponseEntity<TransferInventoryResponse> transfer(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody TransferInventoryRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        TransferInventoryResponse response = inventoryCommandService.transfer(inventoryItemId, request, access);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{inventoryItemId}/in-transit/announce")
    public ResponseEntity<EntityModel<InventoryResponse>> announceInTransit(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody AnnounceInTransitRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.announceInTransit(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/in-transit/receive")
    public ResponseEntity<EntityModel<InventoryResponse>> receiveInTransit(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReceiveInTransitRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.receiveInTransit(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PutMapping("/{inventoryItemId}/reorder-config")
    public ResponseEntity<EntityModel<InventoryResponse>> updateReorderConfig(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody UpdateReorderConfigRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        InventoryResponse response = inventoryCommandService.updateReorderConfig(inventoryItemId, request, access);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @GetMapping("/{inventoryItemId}/movements")
    public ResponseEntity<PagedModel<EntityModel<StockMovementResponse>>> getMovements(
            @PathVariable String inventoryItemId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<StockMovementResponse> pagedAssembler
    ) {
        Page<StockMovementResponse> page = inventoryQueryService.getMovements(inventoryItemId, pageable);
        PagedModel<EntityModel<StockMovementResponse>> pagedModel = pagedAssembler.toModel(page, inventoryMovementModelAssembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{inventoryItemId}/reservations")
    public ResponseEntity<PagedModel<EntityModel<InventoryReservationResponse>>> getReservations(
            @PathVariable String inventoryItemId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<InventoryReservationResponse> pagedAssembler
    ) {
        Page<InventoryReservationResponse> page = inventoryQueryService.getReservations(inventoryItemId, pageable);
        PagedModel<EntityModel<InventoryReservationResponse>> pagedModel = pagedAssembler.toModel(page, inventoryReservationModelAssembler);
        return ResponseEntity.ok(pagedModel);
    }
}
