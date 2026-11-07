package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.ListRecentSellerWorkflowsQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListRecentSellerWorkflowsMapper {

    public ListRecentSellerWorkflowsQuery toQuery(int page, int size) {
        return new ListRecentSellerWorkflowsQuery(page, size);
    }

    public abstract List<WorkflowExecutionResponse> toResponse(List<WorkflowExecutionOutboundResult> results);

    public abstract WorkflowExecutionResponse toResponse(WorkflowExecutionOutboundResult result);
}
