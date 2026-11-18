package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalResult;
import com.grab.service.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AcceptWorkflowSignalPortMapper {

    @Mapping(target = "message", source = "message")
    public abstract AcceptWorkflowSignalCommand toCommand(SignalMessage message);

    public abstract SignalResult toResponse(SignalResult result);
}
