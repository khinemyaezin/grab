package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListRecentWorkflowsMessageMapper {

    public abstract List<WorkflowExecutionOutboundResult> toResultList(List<WorkflowExecutionPort.WorkflowExecutionResult> results);

    public abstract WorkflowExecutionOutboundResult toResult(WorkflowExecutionPort.WorkflowExecutionResult result);
}

