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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerWorkflowQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class SellerSellableProductWorkflowController {

    private final SellerWorkflowCommandService commandService;
    private final SellerWorkflowQueryService queryService;
    private final CreateSellableProductModelAssembler createModelAssembler;
    private final UpdateSellableProductModelAssembler updateModelAssembler;
    private final UpdateProductVariantModelAssembler variantModelAssembler;
    private final WorkflowExecutionModelAssembler genericModelAssembler;

    @PostMapping("/create-sellable-product")
    @Operation(description = "HAL relation create-sellable-product starts the workflow. Follow get-create-sellable-product to poll.")
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> startCreateSellableProduct(
            @Valid @RequestBody CreateSellableProductRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        CreateSellableProductResponse response = commandService.startCreateSellableProduct(
                principal.requiredScopeId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createModelAssembler.toCommandModel(response));
    }

    @GetMapping("/create-sellable-product/{workflowId}")
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> getCreateSellableProduct(
            @PathVariable String workflowId) {
        CreateSellableProductResponse response = queryService.getCreateSellableProduct(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(createModelAssembler.toModel(response));
    }

    @PostMapping("/update-sellable-product")
    @Operation(description = "HAL relation update-sellable-product starts the workflow. Follow get-update-sellable-product to poll.")
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> startUpdateSellableProduct(
            @Valid @RequestBody UpdateSellableProductRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        UpdateSellableProductResponse response = commandService.startUpdateSellableProduct(
                principal.requiredScopeId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updateModelAssembler.toCommandModel(response));
    }

    @GetMapping("/update-sellable-product/{workflowId}")
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> getUpdateSellableProduct(
            @PathVariable String workflowId) {
        UpdateSellableProductResponse response = queryService.getUpdateSellableProduct(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updateModelAssembler.toModel(response));
    }

    @PostMapping("/update-product-variant")
    @Operation(description = "HAL relation update-product-variant starts the workflow. Follow get-update-product-variant to poll.")
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> startUpdateProductVariant(
            @Valid @RequestBody UpdateProductVariantRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        UpdateProductVariantResponse response = commandService.startUpdateProductVariant(
                principal.requiredScopeId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(variantModelAssembler.toCommandModel(response));
    }

    @GetMapping("/update-product-variant/{workflowId}")
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> getUpdateProductVariant(
            @PathVariable String workflowId) {
        UpdateProductVariantResponse response = queryService.getUpdateProductVariant(workflowId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variantModelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<WorkflowExecutionResponse>> getWorkflowStatus(
            @PathVariable String workflowId) {
        return queryService.getWorkflowStatus(workflowId)
                .map(genericModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<WorkflowExecutionResponse>>> listRecentWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(genericModelAssembler.listRecentWorkflowsModel(
                queryService.listRecentWorkflows(page, size), page, size));
    }
}
