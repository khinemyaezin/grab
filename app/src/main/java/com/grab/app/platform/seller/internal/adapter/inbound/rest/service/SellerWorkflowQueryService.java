package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerWorkflowStatusMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListRecentSellerWorkflowsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetSellerWorkflowStatusQuery;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.ListRecentSellerWorkflowsQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerWorkflowQueryService {

    private final QueryBus queryBus;
    private final GetSellerWorkflowStatusMapper getStatusMapper;
    private final ListRecentSellerWorkflowsMapper listWorkflowsMapper;

    public SellerWorkflowQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerWorkflowStatusMapper getStatusMapper,
            ListRecentSellerWorkflowsMapper listWorkflowsMapper
    ) {
        this.queryBus = queryBus;
        this.getStatusMapper = getStatusMapper;
        this.listWorkflowsMapper = listWorkflowsMapper;
    }

    public Optional<WorkflowExecutionResponse> getWorkflowStatus(String workflowIdStr) {
        GetSellerWorkflowStatusQuery query = getStatusMapper.toQuery(workflowIdStr);
        Optional<WorkflowExecutionOutboundResult> result = queryBus.dispatch(query);
        return result.map(getStatusMapper::toResponse);
    }

    public List<WorkflowExecutionResponse> listRecentWorkflows(int page, int size) {
        ListRecentSellerWorkflowsQuery query = listWorkflowsMapper.toQuery(page, size);
        List<WorkflowExecutionOutboundResult> results = queryBus.dispatch(query);
        return listWorkflowsMapper.toResponse(results);
    }
}
