package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.controller;

import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.assembler.UpdateProductVariantModelAssembler;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request.UpdateProductVariantRequest;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.response.UpdateProductVariantResponse;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.service.UpdateProductVariantWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows/update-product-variant")
@RequiredArgsConstructor
public class UpdateProductVariantController {

    private final UpdateProductVariantWorkflowService workflowService;
    private final UpdateProductVariantModelAssembler modelAssembler;
    private final WorkflowSellerAccessResolver accessResolver;

    @PostMapping
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> start(
            @Valid @RequestBody UpdateProductVariantRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        WorkflowSellerAccessResolver.WorkflowAccess access = accessResolver.resolve(principal);
        UpdateProductVariantResponse response = workflowService.start(request, access);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<UpdateProductVariantResponse>> get(
            @PathVariable String workflowId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        accessResolver.resolve(principal);
        UpdateProductVariantResponse response = workflowService.get(workflowId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
