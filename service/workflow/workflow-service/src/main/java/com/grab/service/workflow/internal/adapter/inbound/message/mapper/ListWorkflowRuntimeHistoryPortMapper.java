package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowHistoryEntry;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeHistoryQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListWorkflowRuntimeHistoryPortMapper {

    public abstract ListWorkflowRuntimeHistoryQuery toQuery(Id executionId, String callerScope, int page, int size);

    public abstract WorkflowHistoryEntry toResponse(WorkflowHistoryEntry result);
}
