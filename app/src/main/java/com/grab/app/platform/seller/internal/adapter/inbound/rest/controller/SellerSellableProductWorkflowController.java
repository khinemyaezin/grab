package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.WorkflowExecutionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowQueryService;
import com.grab.app.platform.seller.port.rest.SellerWorkflowResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seller/v1/workflows")
public class SellerSellableProductWorkflowController implements SellerWorkflowResource {

    private final SellerWorkflowCommandService commandService;
    private final SellerWorkflowQueryService queryService;
    private final WorkflowExecutionModelAssembler modelAssembler;

    public SellerSellableProductWorkflowController(
            SellerWorkflowCommandService commandService,
            SellerWorkflowQueryService queryService,
            WorkflowExecutionModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping("/products/sellable")
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> startCreateSellableProduct(
            @Valid @RequestBody CreateSellableProductWorkflowRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        WorkflowExecutionResponse response = commandService.startCreateSellableProduct(merchantId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.workflowId())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @PutMapping("/products/sellable/{productId}")
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> startUpdateSellableProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateSellableProductWorkflowRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        WorkflowExecutionResponse response = commandService.startUpdateSellableProduct(productId, merchantId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PutMapping("/products/sellable/{productId}/variants/{variantId}")
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> startUpdateProductVariant(
            @PathVariable String productId,
            @PathVariable String variantId,
            @Valid @RequestBody UpdateProductVariantWorkflowRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        WorkflowExecutionResponse response = commandService.startUpdateProductVariant(productId, variantId, merchantId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> getWorkflowStatus(
            @PathVariable String workflowId) {
        return queryService.getWorkflowStatus(workflowId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<WorkflowExecutionResponse>>> listRecentWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityModel<WorkflowExecutionResponse>> list = queryService.listRecentWorkflows(page, size).stream()
                .map(modelAssembler::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(list));
    }
}

