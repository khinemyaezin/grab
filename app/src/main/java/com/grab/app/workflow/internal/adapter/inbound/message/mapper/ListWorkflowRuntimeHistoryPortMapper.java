package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeHistoryQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort.WorkflowHistoryEntry;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeHistoryPortMapper {

    public abstract ListWorkflowRuntimeHistoryQuery toQuery(Id executionId, String callerScope, int page, int size);

    public abstract WorkflowHistoryEntry toResponse(WorkflowHistoryEntry result);
}
