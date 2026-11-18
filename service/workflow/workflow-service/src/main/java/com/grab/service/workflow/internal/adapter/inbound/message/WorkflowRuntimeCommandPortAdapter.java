package com.grab.service.workflow.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.exception.DomainException;
import com.grab.rpc.adapter.RmiExport;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort;
import com.grab.service.workflow.internal.adapter.inbound.message.mapper.CancelWorkflowRuntimePortMapper;
import com.grab.service.workflow.internal.adapter.inbound.message.mapper.RetryWorkflowIncidentPortMapper;
import com.grab.service.workflow.internal.adapter.inbound.message.mapper.StartWorkflowRuntimePortMapper;
import com.grab.service.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.service.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.error.WorkflowServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
@RmiExport
public class WorkflowRuntimeCommandPortAdapter implements WorkflowRuntimeCommandPort {

    private final CommandBus commandBus;
    private final StartWorkflowRuntimePortMapper startMapper;
    private final CancelWorkflowRuntimePortMapper requestCancellationMapper;
    private final RetryWorkflowIncidentPortMapper retryIncidentMapper;

    public WorkflowRuntimeCommandPortAdapter(
            @Qualifier("workflowCommandBus") CommandBus commandBus,
            StartWorkflowRuntimePortMapper startMapper,
            CancelWorkflowRuntimePortMapper requestCancellationMapper,
            RetryWorkflowIncidentPortMapper retryIncidentMapper) {
        this.commandBus = commandBus;
        this.startMapper = startMapper;
        this.requestCancellationMapper = requestCancellationMapper;
        this.retryIncidentMapper = retryIncidentMapper;
    }

    @Override
    public StartResult start(StartMessage message) throws RemoteException {
        StartWorkflowRuntimeCommand command = startMapper.toCommand(message);
        try {
            var result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
            return startMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    @Override
    public OperationResult requestCancellation(CancelMessage message) throws RemoteException {
        CancelWorkflowRuntimeCommand command = requestCancellationMapper.toCommand(message);
        try {
            var result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
            return requestCancellationMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    @Override
    public OperationResult retryIncident(RetryIncidentMessage message) throws RemoteException {
        RetryWorkflowIncidentCommand command = retryIncidentMapper.toCommand(message);
        try {
            var result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
            return retryIncidentMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    private RemoteException toRemoteException(DomainException exception) {
        String code = exception.getMessageSource() != null ? exception.getMessageSource().code() : "workflow.error";
        return new RemoteException(code + ": " + exception.getMessage(), exception);
    }
}
