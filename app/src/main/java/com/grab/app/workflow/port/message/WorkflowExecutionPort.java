package com.grab.app.workflow.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Universal Java Port Interface for Multi-Domain Saga Workflow Execution.
 * Decouples cross-domain sagas from HTTP controllers.
 */
public interface WorkflowExecutionPort {

    WorkflowExecutionResult startCreateSellableProduct(CreateSellableProductWorkflowMessage message);

    WorkflowExecutionResult startUpdateSellableProduct(Id productId, UpdateSellableProductWorkflowMessage message);

    WorkflowExecutionResult startUpdateProductVariant(Id productId, Id variantId, UpdateProductVariantWorkflowMessage message);

    record CreateSellableProductWorkflowMessage(
            Id merchantId,
            String title,
            String sku,
            BigDecimal price,
            String currencyCode,
            int initialStock,
            Id locationId,
            List<Id> salesChannelIds,
            Map<String, String> attributes
    ) {}

    record UpdateSellableProductWorkflowMessage(
            Id merchantId,
            String title,
            String description,
            Id categoryId,
            List<Id> publishSalesChannelIds,
            List<Id> unpublishSalesChannelIds,
            Map<String, String> attributes
    ) {}

    record UpdateProductVariantWorkflowMessage(
            Id merchantId,
            String title,
            BigDecimal price,
            String currencyCode,
            int stockDelta,
            Map<String, String> optionValues
    ) {}

    record WorkflowExecutionResult(
            Id workflowId,
            String workflowName,
            String status, // STARTED, RUNNING, COMPLETED, FAILED, COMPENSATED
            Id targetEntityId,
            String currentStep,
            Instant startedAt,
            Instant completedAt
    ) {}
}

