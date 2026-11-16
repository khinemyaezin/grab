package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.error.WorkflowServiceException;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.StartWorkflowRuntimePortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.CancelWorkflowRuntimePortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.RetryWorkflowIncidentPortMapper;
import com.grab.app.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.app.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.app.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRuntimeCommandPortAdapter implements WorkflowRuntimeCommandPort {

    private final CommandBus commandBus;
    private final StartWorkflowRuntimePortMapper startMapper;
    private final CancelWorkflowRuntimePortMapper requestCancellationMapper;
    private final RetryWorkflowIncidentPortMapper retryIncidentMapper;

    public WorkflowRuntimeCommandPortAdapter(@Qualifier("workflowCommandBus") CommandBus commandBus,
            StartWorkflowRuntimePortMapper startMapper,
            CancelWorkflowRuntimePortMapper requestCancellationMapper,
            RetryWorkflowIncidentPortMapper retryIncidentMapper) {
        this.commandBus = commandBus;
        this.startMapper = startMapper;
        this.requestCancellationMapper = requestCancellationMapper;
        this.retryIncidentMapper = retryIncidentMapper;
    }

    @Override
    public StartResult start(StartMessage message) {
        StartWorkflowRuntimeCommand command = startMapper.toCommand(message);
        StartResult result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
        return startMapper.toResponse(result);
    }

    @Override
    public OperationResult requestCancellation(CancelMessage message) {
        CancelWorkflowRuntimeCommand command = requestCancellationMapper.toCommand(message);
        OperationResult result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
        return requestCancellationMapper.toResponse(result);
    }

    @Override
    public OperationResult retryIncident(RetryIncidentMessage message) {
        RetryWorkflowIncidentCommand command = retryIncidentMapper.toCommand(message);
        OperationResult result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
        return retryIncidentMapper.toResponse(result);
    }
}
