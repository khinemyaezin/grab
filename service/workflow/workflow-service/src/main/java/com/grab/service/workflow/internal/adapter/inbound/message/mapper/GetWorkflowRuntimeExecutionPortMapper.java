package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowExecutionSummary;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import com.grab.service.workflow.internal.query.model.GetWorkflowRuntimeExecutionQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetWorkflowRuntimeExecutionPortMapper {

    public abstract GetWorkflowRuntimeExecutionQuery toQuery(Id executionId, String callerScope);

    public abstract WorkflowExecutionSummary toResponse(WorkflowExecutionSummary result);
}
