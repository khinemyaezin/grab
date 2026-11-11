package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.AllocationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import com.grab.app.platform.seller.port.rest.SellerInventoryResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/allocations")
public class SellerAllocationController implements SellerInventoryResource.Allocations {

    private final SellerInventoryCommandService commandService;
    private final AllocationModelAssembler modelAssembler;

    public SellerAllocationController(
            SellerInventoryCommandService commandService,
            AllocationModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<AllocationResponse>> allocateStock(
            @Valid @RequestBody AllocateStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        AllocationResponse response = commandService.allocateStock(actorId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PostMapping("/deallocate")
    public ResponseEntity<Void> deallocateStock(
            @Valid @RequestBody DeallocateStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        commandService.deallocateStock(actorId, request);
        return ResponseEntity.noContent().build();
    }
}
