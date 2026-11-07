package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetSellerWorkflowStatusQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerWorkflowStatusQueryHandler
        implements QueryHandler<GetSellerWorkflowStatusQuery, Optional<WorkflowExecutionOutboundResult>> {

    private final SellerWorkflowOutboundPort outboundPort;

    public GetSellerWorkflowStatusQueryHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<WorkflowExecutionOutboundResult> handle(GetSellerWorkflowStatusQuery query) {
        return outboundPort.getWorkflowStatus(query.workflowId());
    }

    @Override
    public Class<GetSellerWorkflowStatusQuery> getQueryType() {
        return GetSellerWorkflowStatusQuery.class;
    }
}

