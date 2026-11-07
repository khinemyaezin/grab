package com.grab.app.platform.seller.port.outbound;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Multi-Domain Saga Workflows.
 */
public interface SellerWorkflowOutboundPort {

    CreateSellableProductResponse startCreateSellableProduct(CreateSellableProductMessage message);

    CreateSellableProductResponse getCreateSellableProduct(Id workflowId);

    UpdateSellableProductResponse startUpdateSellableProduct(UpdateSellableProductMessage message);

    UpdateSellableProductResponse getUpdateSellableProduct(Id workflowId);

    UpdateProductVariantResponse startUpdateProductVariant(UpdateProductVariantMessage message);

    UpdateProductVariantResponse getUpdateProductVariant(Id workflowId);

    Optional<WorkflowExecutionOutboundResult> getWorkflowStatus(Id workflowId);

    List<WorkflowExecutionOutboundResult> listRecentWorkflows(int page, int size);

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
