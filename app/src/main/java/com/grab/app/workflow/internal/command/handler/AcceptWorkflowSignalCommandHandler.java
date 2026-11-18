package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.app.workflow.internal.port.inbound.WorkflowRuntimeSignalUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class AcceptWorkflowSignalCommandHandler
        implements CommandHandler<AcceptWorkflowSignalCommand, WorkflowRuntimeSignalPort.SignalResult> {

    private final WorkflowRuntimeSignalUseCase service;

    public AcceptWorkflowSignalCommandHandler(WorkflowRuntimeSignalUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowRuntimeSignalPort.SignalResult handle(AcceptWorkflowSignalCommand command) {
        return service.accept(command.message());
    }

    @Override
    public Class<AcceptWorkflowSignalCommand> getCommandType() {
        return AcceptWorkflowSignalCommand.class;
    }
}
