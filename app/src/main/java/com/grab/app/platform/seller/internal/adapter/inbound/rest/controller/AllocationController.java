package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.assembler.AllocationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.AllocateStockResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.AllocationAvailabilityResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.DeallocateStockResponse;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.command.InventoryCommandService;
import com.grab.app.platform.seller.internal.query.InventoryQueryService;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;
import com.grab.app.shared.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/allocations")
@RequiredArgsConstructor
public class AllocationController implements SellerRestApi {

    private final InventoryCommandService inventoryCommandService;
    private final InventoryQueryService inventoryQueryService;
    private final AllocationModelAssembler allocationModelAssembler;
    private final AuthenticatedInventoryScopeResolver scopeResolver;

    @PostMapping
    public ResponseEntity<EntityModel<AllocateStockResponse>> allocate(
            @Valid @RequestBody AllocateStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        AllocateStockResponse response = inventoryCommandService.allocateStock(request, access);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(allocationModelAssembler.toAllocateModel(response));
    }

    @PostMapping("/deallocate")
    public ResponseEntity<EntityModel<DeallocateStockResponse>> deallocate(
            @Valid @RequestBody DeallocateStockRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        ResolvedInventoryAccess access = scopeResolver.resolve(principal);
        DeallocateStockResponse response = inventoryCommandService.deallocateStock(request, access);
        return ResponseEntity.ok(allocationModelAssembler.toDeallocateModel(response));
    }

    @GetMapping("/availability")
    public ResponseEntity<EntityModel<AllocationAvailabilityResponse>> availability(
            @RequestParam String sku,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) String salesChannelId
    ) {
        AllocationAvailabilityResponse response = inventoryQueryService.getAllocationAvailability(sku, quantity, salesChannelId);
        return ResponseEntity.ok(allocationModelAssembler.toAvailabilityModel(response));
    }
}
