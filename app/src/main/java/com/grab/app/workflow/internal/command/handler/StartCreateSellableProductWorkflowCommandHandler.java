package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.StartCreateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.workflows.createsellableproduct.service.CreateSellableProductWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartCreateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartCreateSellableProductWorkflowCommand, CreateSellableProductResult> {

    private final CreateSellableProductWorkflowService service;

    public StartCreateSellableProductWorkflowCommandHandler(CreateSellableProductWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowTransactional
    public CreateSellableProductResult handle(StartCreateSellableProductWorkflowCommand command) {
        return service.start(command);
    }

    @Override
    public Class<StartCreateSellableProductWorkflowCommand> getCommandType() {
        return StartCreateSellableProductWorkflowCommand.class;
    }
}
