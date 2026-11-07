package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.service.UpdateSellableProductWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartUpdateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartUpdateSellableProductWorkflowCommand, UpdateSellableProductResult> {

    private final UpdateSellableProductWorkflowService service;

    public StartUpdateSellableProductWorkflowCommandHandler(UpdateSellableProductWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowTransactional
    public UpdateSellableProductResult handle(StartUpdateSellableProductWorkflowCommand command) {
        return service.start(command);
    }

    @Override
    public Class<StartUpdateSellableProductWorkflowCommand> getCommandType() {
        return StartUpdateSellableProductWorkflowCommand.class;
    }
}
