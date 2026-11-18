package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.CancelMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CancelWorkflowRuntimePortMapper {

    @Mapping(target = "message", source = "message")
    public abstract CancelWorkflowRuntimeCommand toCommand(CancelMessage message);

    public abstract OperationResult toResponse(OperationResult result);
}
