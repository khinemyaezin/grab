package com.grab.app.platform.seller.port.outbound;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Seller Outbound Port for Multi-Domain Saga Workflows.
 */
public interface SellerWorkflowOutboundPort {

    WorkflowExecutionOutboundResult startCreateSellableProduct(CreateSellableProductRecord record);

    WorkflowExecutionOutboundResult startUpdateSellableProduct(Id productId, UpdateSellableProductRecord record);

    WorkflowExecutionOutboundResult startUpdateProductVariant(Id productId, Id variantId, UpdateProductVariantRecord record);

    Optional<WorkflowExecutionOutboundResult> getWorkflowStatus(Id workflowId);

    List<WorkflowExecutionOutboundResult> listRecentWorkflows(int page, int size);

    record CreateSellableProductRecord(
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

    record UpdateSellableProductRecord(
            Id merchantId,
            String title,
            String description,
            Id categoryId,
            List<Id> publishSalesChannelIds,
            List<Id> unpublishSalesChannelIds,
            Map<String, String> attributes
    ) {}

    record UpdateProductVariantRecord(
            Id merchantId,
            String title,
            BigDecimal price,
            String currencyCode,
            int stockDelta,
            Map<String, String> optionValues
    ) {}

    record WorkflowExecutionOutboundResult(
            String workflowId,
            String workflowName,
            String status,
            String targetEntityId,
            String currentStep,
            Instant startedAt,
            Instant completedAt
    ) {}
}

