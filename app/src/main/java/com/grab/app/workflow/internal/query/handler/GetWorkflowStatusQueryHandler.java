package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.GetWorkflowStatusQuery;
import com.grab.app.workflow.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.app.workflow.internal.workflows.createsellableproduct.CreateSellableProductWorkflowNames;
import com.grab.app.workflow.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.app.workflow.internal.workflows.updateproductvariant.UpdateProductVariantWorkflowNames;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.UpdateSellableProductWorkflowNames;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.WorkflowExecutionResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetWorkflowStatusQueryHandler implements QueryHandler<GetWorkflowStatusQuery, Optional<WorkflowExecutionResult>> {

    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;

    public GetWorkflowStatusQueryHandler(WorkflowStore workflowStore, WorkflowPayloadCodec payloadCodec) {
        this.workflowStore = workflowStore;
        this.payloadCodec = payloadCodec;
    }

    @Override
    @WorkflowReadTransactional
    public Optional<WorkflowExecutionResult> handle(GetWorkflowStatusQuery query) {
        return workflowStore.findById(query.workflowId().getValue())
                .map(this::toResult);
    }

    private WorkflowExecutionResult toResult(WorkflowInstance instance) {
        boolean isTerminal = instance.status() == WorkflowStatus.COMPLETED
                || instance.status() == WorkflowStatus.FAILED
                || instance.status() == WorkflowStatus.COMPENSATED;

        Id targetEntityId = resolveTargetEntityId(instance);

        return new WorkflowExecutionResult(
                new CommonId(instance.id()),
                instance.workflowName(),
                instance.status().name(),
                targetEntityId,
                instance.currentStep().orElse(null),
                instance.createdAt(),
                isTerminal ? instance.updatedAt() : null
        );
    }

    private Id resolveTargetEntityId(WorkflowInstance instance) {
        return instance.contextJson().map(json -> {
            switch (instance.workflowName()) {
                case CreateSellableProductWorkflowNames.WORKFLOW_NAME -> {
                    CreateSellableProductContext ctx = payloadCodec.readTyped(json, CreateSellableProductContext.class);
                    return ctx != null && ctx.productId() != null ? new CommonId(ctx.productId()) : null;
                }
                case UpdateSellableProductWorkflowNames.WORKFLOW_NAME -> {
                    UpdateSellableProductContext ctx = payloadCodec.readTyped(json, UpdateSellableProductContext.class);
                    return ctx != null && ctx.productId() != null ? new CommonId(ctx.productId()) : null;
                }
                case UpdateProductVariantWorkflowNames.WORKFLOW_NAME -> {
                    UpdateProductVariantContext ctx = payloadCodec.readTyped(json, UpdateProductVariantContext.class);
                    return ctx != null && ctx.variantId() != null ? new CommonId(ctx.variantId()) : null;
                }
                default -> {
                    return null;
                }
            }
        }).orElse(null);
    }

    @Override
    public Class<GetWorkflowStatusQuery> getQueryType() {
        return GetWorkflowStatusQuery.class;
    }
}
