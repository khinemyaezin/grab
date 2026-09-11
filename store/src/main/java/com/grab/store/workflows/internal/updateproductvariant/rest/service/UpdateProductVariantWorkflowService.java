package com.grab.store.workflows.internal.updateproductvariant.rest.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.updateproductvariant.UpdateProductVariantContext;
import com.grab.store.workflows.internal.updateproductvariant.UpdateProductVariantOrchestrator;
import com.grab.store.workflows.internal.updateproductvariant.rest.dto.request.UpdateProductVariantRequest;
import com.grab.store.workflows.internal.updateproductvariant.rest.dto.response.UpdateProductVariantResponse;
import com.grab.store.workflows.internal.updateproductvariant.rest.mapper.UpdateProductVariantRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProductVariantWorkflowService {

    private final UpdateProductVariantOrchestrator orchestrator;
    private final UpdateProductVariantRequestMapper mapper;

    public UpdateProductVariantResponse start(
            UpdateProductVariantRequest request,
            WorkflowSellerAccessResolver.WorkflowAccess access
    ) {
        UpdateProductVariantContext context = mapper.toContext(
                request,
                access.merchantId(),
                access.actorId(),
                access.scopeKey(),
                access.scopeId()
        );
        WorkflowInstance instance = orchestrator.start(context, request.idempotencyKey());
        UpdateProductVariantContext savedContext = orchestrator.readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public UpdateProductVariantResponse get(String workflowId) {
        WorkflowInstance instance = orchestrator.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        UpdateProductVariantContext context = orchestrator.readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }
}
