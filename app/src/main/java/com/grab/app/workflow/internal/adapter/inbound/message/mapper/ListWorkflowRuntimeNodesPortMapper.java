package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeNodesQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort.WorkflowNodeView;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeNodesPortMapper {

    public abstract ListWorkflowRuntimeNodesQuery toQuery(Id executionId, String callerScope);

    public abstract WorkflowNodeView toResponse(WorkflowNodeView result);
}
