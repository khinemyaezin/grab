package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.RetryIncidentMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RetryWorkflowIncidentPortMapper {

    @Mapping(target = "message", source = "message")
    public abstract RetryWorkflowIncidentCommand toCommand(RetryIncidentMessage message);

    public abstract OperationResult toResponse(OperationResult result);
}
