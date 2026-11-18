package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.app.workflow.internal.port.inbound.CancelWorkflowUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class CancelWorkflowRuntimeCommandHandler
        implements CommandHandler<CancelWorkflowRuntimeCommand, WorkflowRuntimeCommandPort.OperationResult> {

    private final CancelWorkflowUseCase service;

    public CancelWorkflowRuntimeCommandHandler(CancelWorkflowUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowRuntimeCommandPort.OperationResult handle(CancelWorkflowRuntimeCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<CancelWorkflowRuntimeCommand> getCommandType() {
        return CancelWorkflowRuntimeCommand.class;
    }
}
