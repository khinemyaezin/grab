package com.grab.service.workflow.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartResult;
import com.grab.service.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.service.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.service.workflow.internal.config.WorkflowTransactional;
import com.grab.service.workflow.internal.port.inbound.StartWorkflowUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class StartWorkflowRuntimeCommandHandler
        implements CommandHandler<StartWorkflowRuntimeCommand, StartResult> {

    private final StartWorkflowUseCase service;

    public StartWorkflowRuntimeCommandHandler(StartWorkflowUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public StartResult handle(StartWorkflowRuntimeCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<StartWorkflowRuntimeCommand> getCommandType() {
        return StartWorkflowRuntimeCommand.class;
    }
}
