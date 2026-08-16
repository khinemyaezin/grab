package com.grab.store.workflows.internal.updatesellableproduct.rest.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.updatesellableproduct.UpdateSellableProductOrchestrator;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
import com.grab.store.workflows.internal.updatesellableproduct.rest.mapper.UpdateSellableProductRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateSellableProductWorkflowService {

    private final UpdateSellableProductOrchestrator orchestrator;
    private final UpdateSellableProductRequestMapper mapper;

    public UpdateSellableProductResponse start(
            UpdateSellableProductRequest request,
            WorkflowSellerAccessResolver.WorkflowAccess access
    ) {
        UpdateSellableProductContext context = mapper.toContext(
                request,
                access.merchantId(),
                access.actorId(),
                access.scopeKey(),
                access.scopeId()
        );
        WorkflowInstance instance = orchestrator.start(context, request.idempotencyKey());
        UpdateSellableProductContext savedContext = orchestrator.readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public UpdateSellableProductResponse get(String workflowId) {
        WorkflowInstance instance = orchestrator.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        UpdateSellableProductContext context = orchestrator.readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }
}
