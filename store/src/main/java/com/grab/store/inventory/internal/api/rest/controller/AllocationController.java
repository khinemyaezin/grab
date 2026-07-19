package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.AllocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AllocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.DeallocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocationAvailabilityResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.DeallocateStockResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.InventoryCommandService;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import com.grab.store.shared.security.SecurityPrincipal;
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
public class AllocationController {

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
            @RequestParam(required = false) Integer quantity
    ) {
        AllocationAvailabilityResponse response = inventoryQueryService.getAllocationAvailability(sku, quantity);
        return ResponseEntity.ok(allocationModelAssembler.toAvailabilityModel(response));
    }
}
