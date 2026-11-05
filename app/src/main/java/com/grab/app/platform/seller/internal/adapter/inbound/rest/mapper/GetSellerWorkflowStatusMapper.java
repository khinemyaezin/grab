package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerWorkflowQueries.GetSellerWorkflowStatusQuery;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSellerWorkflowStatusMapper {

    public abstract GetSellerWorkflowStatusQuery toQuery(String workflowId);

    public abstract WorkflowExecutionResponse toResponse(WorkflowExecutionOutboundResult result);
}

