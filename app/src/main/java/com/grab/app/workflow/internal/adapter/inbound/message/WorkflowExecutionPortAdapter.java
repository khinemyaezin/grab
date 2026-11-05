package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.adapter.inbound.message.mapper.CreateSellableProductWorkflowPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.UpdateProductVariantWorkflowPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.UpdateSellableProductWorkflowPortMapper;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WorkflowExecutionPortAdapter implements WorkflowExecutionPort {

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CreateSellableProductWorkflowPortMapper createWorkflowMapper;
    private final UpdateSellableProductWorkflowPortMapper updateProductWorkflowMapper;
    private final UpdateProductVariantWorkflowPortMapper updateVariantWorkflowMapper;

    public WorkflowExecutionPortAdapter(
            @Qualifier("workflowCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            CreateSellableProductWorkflowPortMapper createWorkflowMapper,
            UpdateSellableProductWorkflowPortMapper updateProductWorkflowMapper,
            UpdateProductVariantWorkflowPortMapper updateVariantWorkflowMapper) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
        this.createWorkflowMapper = createWorkflowMapper;
        this.updateProductWorkflowMapper = updateProductWorkflowMapper;
        this.updateVariantWorkflowMapper = updateVariantWorkflowMapper;
    }

    @Override
    public WorkflowExecutionResult startCreateSellableProduct(CreateSellableProductWorkflowMessage message) {
        Id workflowId = idGenerator.generateId();
        return new WorkflowExecutionResult(
                workflowId,
                "create-sellable-product",
                "STARTED",
                null,
                "STEP_CREATE_PRODUCT",
                Instant.now(),
                null
        );
    }

    @Override
    public WorkflowExecutionResult startUpdateSellableProduct(Id productId, UpdateSellableProductWorkflowMessage message) {
        Id workflowId = idGenerator.generateId();
        return new WorkflowExecutionResult(
                workflowId,
                "update-sellable-product",
                "STARTED",
                productId,
                "STEP_ASSERT_PRODUCT",
                Instant.now(),
                null
        );
    }

    @Override
    public WorkflowExecutionResult startUpdateProductVariant(Id productId, Id variantId, UpdateProductVariantWorkflowMessage message) {
        Id workflowId = idGenerator.generateId();
        return new WorkflowExecutionResult(
                workflowId,
                "update-product-variant",
                "STARTED",
                variantId,
                "STEP_SYNC_VARIANT_PRICE",
                Instant.now(),
                null
        );
    }
}
