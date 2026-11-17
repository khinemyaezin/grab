package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowNodeView;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeNodesQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeNodesPortMapper {

    public abstract ListWorkflowRuntimeNodesQuery toQuery(Id executionId, String callerScope);

    public abstract WorkflowNodeView toResponse(WorkflowNodeView result);
}
