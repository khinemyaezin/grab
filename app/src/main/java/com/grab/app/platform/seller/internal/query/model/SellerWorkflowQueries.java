package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public final class SellerWorkflowQueries {

    private SellerWorkflowQueries() {}

    public record GetCreateSellableProductWorkflowQuery(
            Id workflowId
    ) implements Query<CreateSellableProductResponse> {}

    public record GetUpdateSellableProductWorkflowQuery(
            Id workflowId
    ) implements Query<UpdateSellableProductResponse> {}

    public record GetUpdateProductVariantWorkflowQuery(
            Id workflowId
    ) implements Query<UpdateProductVariantResponse> {}

    public record GetSellerWorkflowStatusQuery(
            Id workflowId
    ) implements Query<Optional<WorkflowExecutionOutboundResult>> {}

    public record ListRecentSellerWorkflowsQuery(
            int page,
            int size
    ) implements Query<List<WorkflowExecutionOutboundResult>> {}
}
