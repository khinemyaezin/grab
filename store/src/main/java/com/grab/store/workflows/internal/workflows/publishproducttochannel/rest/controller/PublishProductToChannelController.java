package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.controller;

import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.assembler.PublishProductToChannelModelAssembler;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.request.PublishProductToChannelRequest;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.response.PublishProductToChannelResponse;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.service.PublishProductToChannelWorkflowService;
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
@RequestMapping("/api/v1/workflows/publish-product-to-channel")
@RequiredArgsConstructor
public class PublishProductToChannelController {

    private final PublishProductToChannelWorkflowService workflowService;
    private final PublishProductToChannelModelAssembler modelAssembler;
    private final WorkflowSellerAccessResolver accessResolver;

    @PostMapping
    public ResponseEntity<EntityModel<PublishProductToChannelResponse>> start(
            @Valid @RequestBody PublishProductToChannelRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        WorkflowSellerAccessResolver.WorkflowAccess access = accessResolver.resolve(principal);
        PublishProductToChannelResponse response = workflowService.start(request, access);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<EntityModel<PublishProductToChannelResponse>> get(
            @PathVariable String workflowId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        accessResolver.resolve(principal);
        PublishProductToChannelResponse response = workflowService.get(workflowId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
