package com.grab.store.workflows.internal.updatesellableproduct.rest.controller;

import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.workflows.internal.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.updatesellableproduct.rest.assembler.UpdateSellableProductModelAssembler;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
import com.grab.store.workflows.internal.updatesellableproduct.rest.service.UpdateSellableProductWorkflowService;
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
@RequestMapping("/api/v1/workflows/update-sellable-product")
@RequiredArgsConstructor
public class UpdateSellableProductController {

    private final UpdateSellableProductWorkflowService workflowService;
    private final UpdateSellableProductModelAssembler modelAssembler;
    private final WorkflowSellerAccessResolver accessResolver;

    @PostMapping
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> start(
            @Valid @RequestBody UpdateSellableProductRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        WorkflowSellerAccessResolver.WorkflowAccess access = accessResolver.resolve(principal);
        UpdateSellableProductResponse response = workflowService.start(request, access);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<UpdateSellableProductResponse>> get(
            @PathVariable String workflowId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        accessResolver.resolve(principal);
        UpdateSellableProductResponse response = workflowService.get(workflowId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
