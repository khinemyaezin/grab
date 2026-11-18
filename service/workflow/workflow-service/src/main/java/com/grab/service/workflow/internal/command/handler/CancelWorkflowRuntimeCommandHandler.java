package com.grab.service.workflow.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.workflow.internal.command.model.CancelWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.service.workflow.internal.config.WorkflowTransactional;
import com.grab.service.workflow.internal.port.inbound.CancelWorkflowUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class CancelWorkflowRuntimeCommandHandler
        implements CommandHandler<CancelWorkflowRuntimeCommand, OperationResult> {

    private final CancelWorkflowUseCase service;

    public CancelWorkflowRuntimeCommandHandler(CancelWorkflowUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public OperationResult handle(CancelWorkflowRuntimeCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<CancelWorkflowRuntimeCommand> getCommandType() {
        return CancelWorkflowRuntimeCommand.class;
    }
}
