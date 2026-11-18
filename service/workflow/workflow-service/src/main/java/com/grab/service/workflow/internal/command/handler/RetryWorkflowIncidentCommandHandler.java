package com.grab.service.workflow.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.RetryIncidentMessage;
import com.grab.service.workflow.internal.command.model.RetryWorkflowIncidentCommand;
import com.grab.service.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.service.workflow.internal.config.WorkflowTransactional;
import com.grab.service.workflow.internal.port.inbound.RetryWorkflowIncidentUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RetryWorkflowIncidentCommandHandler
        implements CommandHandler<RetryWorkflowIncidentCommand, OperationResult> {

    private final RetryWorkflowIncidentUseCase service;

    public RetryWorkflowIncidentCommandHandler(RetryWorkflowIncidentUseCase service) {
        this.service = service;
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public OperationResult handle(RetryWorkflowIncidentCommand command) {
        return service.execute(command.message());
    }

    @Override
    public Class<RetryWorkflowIncidentCommand> getCommandType() {
        return RetryWorkflowIncidentCommand.class;
    }
}
