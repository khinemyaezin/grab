package com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.service;

import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.mapper.UpdateSellableProductRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateSellableProductWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowProcess<UpdateSellableProductContext> updateSellableProduct;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
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
        WorkflowInstance instance = workflowEngine.start(
                updateSellableProduct.create(),
                context,
                request.idempotencyKey()
        );
        UpdateSellableProductContext savedContext = readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public UpdateSellableProductResponse get(String workflowId) {
        WorkflowInstance instance = workflowStore.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        UpdateSellableProductContext context = readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }

    private Optional<UpdateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, UpdateSellableProductContext.class));
    }
}
