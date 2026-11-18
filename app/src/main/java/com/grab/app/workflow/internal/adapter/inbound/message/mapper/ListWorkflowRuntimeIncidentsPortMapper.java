package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeIncidentsQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort.WorkflowIncidentView;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeIncidentsPortMapper {

    public abstract ListWorkflowRuntimeIncidentsQuery toQuery(String callerScope, int page, int size);

    public abstract WorkflowIncidentView toResponse(WorkflowIncidentView result);
}
