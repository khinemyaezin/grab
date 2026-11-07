package com.grab.app.workflow.internal.command.handler;

import com.grab.app.workflow.internal.command.model.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.workflows.updateproductvariant.service.UpdateProductVariantWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartUpdateProductVariantWorkflowCommandHandler
        implements CommandHandler<StartUpdateProductVariantWorkflowCommand, UpdateProductVariantResult> {

    private final UpdateProductVariantWorkflowService service;

    public StartUpdateProductVariantWorkflowCommandHandler(UpdateProductVariantWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowTransactional
    public UpdateProductVariantResult handle(StartUpdateProductVariantWorkflowCommand command) {
        return service.start(command);
    }

    @Override
    public Class<StartUpdateProductVariantWorkflowCommand> getCommandType() {
        return StartUpdateProductVariantWorkflowCommand.class;
    }
}
