package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.CreateSellableProductModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.UpdateProductVariantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.UpdateSellableProductModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.WorkflowExecutionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowQueryService;
import com.grab.app.platform.seller.port.rest.SellerWorkflowResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/workflows")
public class SellerSellableProductWorkflowController implements SellerWorkflowResource {

    private final SellerWorkflowCommandService commandService;
    private final SellerWorkflowQueryService queryService;
    private final CreateSellableProductModelAssembler createModelAssembler;
    private final UpdateSellableProductModelAssembler updateModelAssembler;
    private final UpdateProductVariantModelAssembler variantModelAssembler;
    private final WorkflowExecutionModelAssembler genericModelAssembler;

    public SellerSellableProductWorkflowController(
            SellerWorkflowCommandService commandService,
            SellerWorkflowQueryService queryService,
            CreateSellableProductModelAssembler createModelAssembler,
            UpdateSellableProductModelAssembler updateModelAssembler,
            UpdateProductVariantModelAssembler variantModelAssembler,
            WorkflowExecutionModelAssembler genericModelAssembler
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.createModelAssembler = createModelAssembler;
        this.updateModelAssembler = updateModelAssembler;
        this.variantModelAssembler = variantModelAssembler;
        this.genericModelAssembler = genericModelAssembler;
    }

    @PostMapping("/create-sellable-product")
    @Override
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> startCreateSellableProduct(
            @Valid @RequestBody CreateSellableProductRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId
    ) {
        CreateSellableProductResponse response = commandService.startCreateSellableProduct(merchantId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createModelAssembler.toModel(response));
    }

    @GetMapping("/create-sellable-product/{workflowId}")
    @Override
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> getCreateSellableProduct(
            @PathVariable String workflowId
    ) {
        CreateSellableProductResponse response = queryService.getCreateSellableProduct(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(createModelAssembler.toModel(response));
    }

    @PostMapping("/update-sellable-product")
    @Override
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> startUpdateSellableProduct(
            @Valid @RequestBody UpdateSellableProductRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId
    ) {
        UpdateSellableProductResponse response = commandService.startUpdateSellableProduct(merchantId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updateModelAssembler.toModel(response));
    }

    @GetMapping("/update-sellable-product/{workflowId}")
    @Override
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> getUpdateSellableProduct(
            @PathVariable String workflowId
    ) {
        UpdateSellableProductResponse response = queryService.getUpdateSellableProduct(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updateModelAssembler.toModel(response));
    }

    @PostMapping("/update-product-variant")
    @Override
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> startUpdateProductVariant(
            @Valid @RequestBody UpdateProductVariantRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId
    ) {
        UpdateProductVariantResponse response = commandService.startUpdateProductVariant(merchantId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(variantModelAssembler.toModel(response));
    }

    @GetMapping("/update-product-variant/{workflowId}")
    @Override
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> getUpdateProductVariant(
            @PathVariable String workflowId
    ) {
        UpdateProductVariantResponse response = queryService.getUpdateProductVariant(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variantModelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    @Override
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> getWorkflowStatus(
            @PathVariable String workflowId
    ) {
        return queryService.getWorkflowStatus(workflowId)
                .map(genericModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Override
    public ResponseEntity<CollectionModel<EntityModel<WorkflowExecutionResponse>>> listRecentWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<EntityModel<WorkflowExecutionResponse>> list = queryService.listRecentWorkflows(page, size).stream()
                .map(genericModelAssembler::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(list));
    }
}
