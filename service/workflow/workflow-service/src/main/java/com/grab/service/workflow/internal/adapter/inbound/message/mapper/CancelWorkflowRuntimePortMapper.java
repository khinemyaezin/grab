package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.CancelMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CancelWorkflowRuntimePortMapper {

    @Mapping(target = "message", source = "message")
    public abstract CancelWorkflowRuntimeCommand toCommand(CancelMessage message);

    public abstract OperationResult toResponse(OperationResult result);
}
