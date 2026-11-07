package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.ListRecentWorkflowsQuery;
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
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowInstanceJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListRecentWorkflowsQueryHandler implements QueryHandler<ListRecentWorkflowsQuery, List<WorkflowExecutionResult>> {

    private final WorkflowInstanceJpaRepository repository;
    private final WorkflowPayloadCodec payloadCodec;

    public ListRecentWorkflowsQueryHandler(
            WorkflowInstanceJpaRepository repository,
            WorkflowPayloadCodec payloadCodec
    ) {
        this.repository = repository;
        this.payloadCodec = payloadCodec;
    }

    @Override
    @WorkflowReadTransactional
    public List<WorkflowExecutionResult> handle(ListRecentWorkflowsQuery query) {
        int page = Math.max(0, query.page());
        int size = query.size() <= 0 ? 20 : query.size();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(pageRequest)
                .map(this::toResult)
                .getContent();
    }

    private WorkflowExecutionResult toResult(WorkflowInstanceEntity entity) {
        boolean isTerminal = entity.getStatus() == WorkflowStatus.COMPLETED
                || entity.getStatus() == WorkflowStatus.FAILED
                || entity.getStatus() == WorkflowStatus.COMPENSATED;

        Id targetEntityId = resolveTargetEntityId(entity);

        return new WorkflowExecutionResult(
                new CommonId(entity.getId()),
                entity.getWorkflowName(),
                entity.getStatus().name(),
                targetEntityId,
                entity.getCurrentStep(),
                entity.getCreatedAt(),
                isTerminal ? entity.getUpdatedAt() : null
        );
    }

    private Id resolveTargetEntityId(WorkflowInstanceEntity entity) {
        String json = entity.getContextJson();
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            switch (entity.getWorkflowName()) {
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
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Class<ListRecentWorkflowsQuery> getQueryType() {
        return ListRecentWorkflowsQuery.class;
    }
}
