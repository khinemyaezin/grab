package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.service;

import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.response.CreateSellableProductResponse;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.mapper.CreateSellableProductRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateSellableProductWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowProcess<CreateSellableProductContext> createSellableProduct;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
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
        WorkflowInstance instance = workflowEngine.start(
                createSellableProduct.create(),
                context,
                request.idempotencyKey()
        );
        CreateSellableProductContext savedContext = readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public CreateSellableProductResponse get(String workflowId) {
        WorkflowInstance instance = workflowStore.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        CreateSellableProductContext context = readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }

    private java.util.Optional<CreateSellableProductContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, CreateSellableProductContext.class));
    }
}
