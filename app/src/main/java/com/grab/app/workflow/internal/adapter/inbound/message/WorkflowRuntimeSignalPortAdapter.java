package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.error.WorkflowServiceException;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.AcceptWorkflowSignalPortMapper;
import com.grab.app.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRuntimeSignalPortAdapter implements WorkflowRuntimeSignalPort {

    private final CommandBus commandBus;
    private final AcceptWorkflowSignalPortMapper acceptMapper;

    public WorkflowRuntimeSignalPortAdapter(@Qualifier("workflowCommandBus") CommandBus commandBus,
            AcceptWorkflowSignalPortMapper acceptMapper) {
        this.commandBus = commandBus;
        this.acceptMapper = acceptMapper;
    }

    @Override
    public SignalResult accept(SignalMessage message) {
        AcceptWorkflowSignalCommand command = acceptMapper.toCommand(message);
        SignalResult result = WorkflowServiceException.translate(() -> commandBus.dispatch(command));
        return acceptMapper.toResponse(result);
    }
}
