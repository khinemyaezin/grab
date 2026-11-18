package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort.SignalMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort.SignalResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AcceptWorkflowSignalPortMapper {

    @Mapping(target = "message", source = "message")
    public abstract AcceptWorkflowSignalCommand toCommand(SignalMessage message);

    public abstract SignalResult toResponse(SignalResult result);
}
