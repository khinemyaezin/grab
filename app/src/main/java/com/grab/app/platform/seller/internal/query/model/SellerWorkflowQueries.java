package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public final class SellerWorkflowQueries {

    private SellerWorkflowQueries() {}

    public record GetSellerWorkflowStatusQuery(
            Id workflowId
    ) implements Query<Optional<WorkflowExecutionOutboundResult>> {}

    public record ListRecentSellerWorkflowsQuery(
            int page,
            int size
    ) implements Query<List<WorkflowExecutionOutboundResult>> {}
}

