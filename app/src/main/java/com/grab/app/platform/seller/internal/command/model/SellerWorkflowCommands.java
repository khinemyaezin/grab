package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class SellerWorkflowCommands {

    private SellerWorkflowCommands() {}

    public record StartCreateSellableProductWorkflowCommand(
            Id merchantId,
            String title,
            String sku,
            BigDecimal price,
            String currencyCode,
            int initialStock,
            Id locationId,
            List<Id> salesChannelIds,
            Map<String, String> attributes
    ) implements Command<WorkflowExecutionOutboundResult> {}

    public record StartUpdateSellableProductWorkflowCommand(
            Id productId,
            Id merchantId,
            String title,
            String description,
            Id categoryId,
            List<Id> publishSalesChannelIds,
            List<Id> unpublishSalesChannelIds,
            Map<String, String> attributes
    ) implements Command<WorkflowExecutionOutboundResult> {}

    public record StartUpdateProductVariantWorkflowCommand(
            Id productId,
            Id variantId,
            Id merchantId,
            String title,
            BigDecimal price,
            String currencyCode,
            int stockDelta,
            Map<String, String> optionValues
    ) implements Command<WorkflowExecutionOutboundResult> {}
}

