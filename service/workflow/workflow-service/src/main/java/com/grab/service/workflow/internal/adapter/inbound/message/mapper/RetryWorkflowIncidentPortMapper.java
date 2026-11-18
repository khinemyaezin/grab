package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.RetryIncidentMessage;
import com.grab.service.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RetryWorkflowIncidentPortMapper {

    @Mapping(target = "message", source = "message")
    public abstract RetryWorkflowIncidentCommand toCommand(RetryIncidentMessage message);

    public abstract OperationResult toResponse(OperationResult result);
}
