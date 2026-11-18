package com.grab.service.workflow.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalResult;
import com.grab.service.workflow.internal.command.model.AcceptWorkflowSignalCommand;
import com.grab.service.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.service.workflow.internal.config.WorkflowTransactional;
import com.grab.service.workflow.internal.port.inbound.WorkflowRuntimeSignalUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class AcceptWorkflowSignalCommandHandler
        implements CommandHandler<AcceptWorkflowSignalCommand, SignalResult> {

    private final WorkflowRuntimeSignalUseCase service;

    public AcceptWorkflowSignalCommandHandler(WorkflowRuntimeSignalUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public SignalResult handle(AcceptWorkflowSignalCommand command) {
        return service.accept(command.message());
    }

    @Override
    public Class<AcceptWorkflowSignalCommand> getCommandType() {
        return AcceptWorkflowSignalCommand.class;
    }
}
