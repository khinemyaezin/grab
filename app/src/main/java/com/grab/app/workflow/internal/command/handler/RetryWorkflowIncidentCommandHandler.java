package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.app.workflow.internal.port.inbound.RetryWorkflowIncidentUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RetryWorkflowIncidentCommandHandler
        implements CommandHandler<RetryWorkflowIncidentCommand, WorkflowRuntimeCommandPort.OperationResult> {

    private final RetryWorkflowIncidentUseCase service;

    public RetryWorkflowIncidentCommandHandler(RetryWorkflowIncidentUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowRuntimeCommandPort.OperationResult handle(RetryWorkflowIncidentCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<RetryWorkflowIncidentCommand> getCommandType() {
        return RetryWorkflowIncidentCommand.class;
    }
}
