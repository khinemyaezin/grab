package com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.service;

import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.app.shared.exception.SharedErrors;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.createsellableproduct.service.WorkflowSellerAccessResolver;
import com.grab.app.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.dto.request.UpdateProductVariantRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.mapper.UpdateProductVariantRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateProductVariantWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowProcess<UpdateProductVariantContext> updateProductVariant;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
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
        WorkflowInstance instance = workflowEngine.start(
                updateProductVariant.create(),
                context,
                request.idempotencyKey()
        );
        UpdateProductVariantContext savedContext = readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public UpdateProductVariantResponse get(String workflowId) {
        WorkflowInstance instance = workflowStore.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        UpdateProductVariantContext context = readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }

    private Optional<UpdateProductVariantContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, UpdateProductVariantContext.class));
    }
}
