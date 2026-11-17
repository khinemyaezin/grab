package com.grab.service.workflow.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.exception.DomainException;
import com.grab.rpc.adapter.RmiExport;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort;
import com.grab.service.workflow.internal.adapter.inbound.message.mapper.AcceptWorkflowSignalPortMapper;
import com.grab.service.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.service.workflow.internal.error.WorkflowServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
@RmiExport
public class WorkflowRuntimeSignalPortAdapter implements WorkflowRuntimeSignalPort {

    private final CommandBus commandBus;
    private final AcceptWorkflowSignalPortMapper acceptMapper;

    public WorkflowRuntimeSignalPortAdapter(
            @Qualifier("workflowCommandBus") CommandBus commandBus,
            AcceptWorkflowSignalPortMapper acceptMapper) {
        this.commandBus = commandBus;
        this.acceptMapper = acceptMapper;
    }

    @Override
    public SignalResult accept(SignalMessage message) throws RemoteException {
        AcceptWorkflowSignalCommand command = acceptMapper.toCommand(message);
        try {
            var result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
            return acceptMapper.toResponse(result);
        } catch (DomainException exception) {
            String code = exception.getMessageSource() != null ? exception.getMessageSource().code() : "workflow.signal.error";
            throw new RemoteException(code + ": " + exception.getMessage(), exception);
        }
    }
}
