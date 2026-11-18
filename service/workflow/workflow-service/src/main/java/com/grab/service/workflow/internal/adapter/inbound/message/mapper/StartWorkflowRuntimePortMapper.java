package com.grab.service.workflow.internal.adapter.inbound.message.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartResult;
import com.grab.service.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartWorkflowRuntimePortMapper {

    @Mapping(target = "message", source = "message")
    public abstract StartWorkflowRuntimeCommand toCommand(StartMessage message);

    public abstract StartResult toResponse(StartResult result);
}
