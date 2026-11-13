package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.AllocationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/allocations")
@RequiredArgsConstructor
public class SellerAllocationController {

    private final SellerInventoryCommandService commandService;
    private final AllocationModelAssembler modelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<AllocationResponse>> allocateStock(
            @Valid @RequestBody AllocateStockRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        AllocationResponse response = commandService.allocateStock(principal.getPlatformUserId(), request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PostMapping("/deallocate")
    public ResponseEntity<Void> deallocateStock(
            @Valid @RequestBody DeallocateStockRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        commandService.deallocateStock(principal.getPlatformUserId(), request);
        return ResponseEntity.noContent().build();
    }
}
