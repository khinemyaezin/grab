package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.ListRecentSellerWorkflowsQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListRecentSellerWorkflowsQueryHandler
        implements QueryHandler<ListRecentSellerWorkflowsQuery, List<WorkflowExecutionOutboundResult>> {

    private final SellerWorkflowOutboundPort outboundPort;

    public ListRecentSellerWorkflowsQueryHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<WorkflowExecutionOutboundResult> handle(ListRecentSellerWorkflowsQuery query) {
        return outboundPort.listRecentWorkflows(query.page(), query.size());
    }

    @Override
    public Class<ListRecentSellerWorkflowsQuery> getQueryType() {
        return ListRecentSellerWorkflowsQuery.class;
    }
}

