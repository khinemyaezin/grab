package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.StartWorkflowRuntimeCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.app.workflow.internal.port.inbound.StartWorkflowUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class StartWorkflowRuntimeCommandHandler
        implements CommandHandler<StartWorkflowRuntimeCommand, WorkflowRuntimeCommandPort.StartResult> {

    private final StartWorkflowUseCase service;

    public StartWorkflowRuntimeCommandHandler(StartWorkflowUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowRuntimeCommandPort.StartResult handle(StartWorkflowRuntimeCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<StartWorkflowRuntimeCommand> getCommandType() {
        return StartWorkflowRuntimeCommand.class;
    }
}
