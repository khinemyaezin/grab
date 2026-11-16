package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.GetWorkflowRuntimeExecutionQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort.WorkflowExecutionSummary;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetWorkflowRuntimeExecutionPortMapper {

    public abstract GetWorkflowRuntimeExecutionQuery toQuery(Id executionId, String callerScope);

    public abstract WorkflowExecutionSummary toResponse(WorkflowExecutionSummary result);
}
