package com.grab.store.workflows.internal.createsellableproduct.rest.controller;

import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.workflows.internal.createsellableproduct.rest.assembler.CreateSellableProductModelAssembler;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.response.CreateSellableProductResponse;
import com.grab.store.workflows.internal.createsellableproduct.rest.service.CreateSellableProductWorkflowService;
import com.grab.store.workflows.internal.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
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
@RequestMapping("/api/v1/workflows/create-sellable-product")
@RequiredArgsConstructor
public class CreateSellableProductController {

    private final CreateSellableProductWorkflowService workflowService;
    private final CreateSellableProductModelAssembler modelAssembler;
    private final WorkflowSellerAccessResolver accessResolver;

    @PostMapping
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> start(
            @Valid @RequestBody CreateSellableProductRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        WorkflowSellerAccessResolver.WorkflowAccess access = accessResolver.resolve(principal);
        CreateSellableProductResponse response = workflowService.start(request, access);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<CreateSellableProductResponse>> get(
            @PathVariable String workflowId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        accessResolver.resolve(principal);
        CreateSellableProductResponse response = workflowService.get(workflowId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
