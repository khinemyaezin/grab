package com.grab.store.workflows.internal.createsellableproduct.rest.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductOrchestrator;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.response.CreateSellableProductResponse;
import com.grab.store.workflows.internal.createsellableproduct.rest.mapper.CreateSellableProductRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateSellableProductWorkflowService {

    private final CreateSellableProductOrchestrator orchestrator;
    private final CreateSellableProductRequestMapper mapper;

    public CreateSellableProductResponse start(
            CreateSellableProductRequest request,
            WorkflowSellerAccessResolver.WorkflowAccess access
    ) {
        CreateSellableProductContext context = mapper.toContext(
                request,
                access.merchantId(),
                access.actorId(),
                access.scopeKey(),
                access.scopeId()
        );
        WorkflowInstance instance = orchestrator.start(context, request.idempotencyKey());
        CreateSellableProductContext savedContext = orchestrator.readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public CreateSellableProductResponse get(String workflowId) {
        WorkflowInstance instance = orchestrator.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        CreateSellableProductContext context = orchestrator.readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }
}
