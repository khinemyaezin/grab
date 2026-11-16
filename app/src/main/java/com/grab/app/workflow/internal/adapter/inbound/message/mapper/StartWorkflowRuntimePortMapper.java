package com.grab.app.workflow.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.StartMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.StartResult;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartWorkflowRuntimePortMapper {

    @Mapping(target = "message", source = "message")
    public abstract StartWorkflowRuntimeCommand toCommand(StartMessage message);

    public abstract StartResult toResponse(StartResult result);
}
