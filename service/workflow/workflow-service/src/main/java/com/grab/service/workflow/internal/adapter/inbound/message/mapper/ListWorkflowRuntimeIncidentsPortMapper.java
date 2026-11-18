package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowIncidentView;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeIncidentsQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeIncidentsPortMapper {

    public abstract ListWorkflowRuntimeIncidentsQuery toQuery(String callerScope, int page, int size);

    public abstract WorkflowIncidentView toResponse(WorkflowIncidentView result);
}
