package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.command.model.StartCreateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.command.model.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.workflow.internal.command.model.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.workflow.internal.query.model.GetCreateSellableProductWorkflowQuery;
import com.grab.app.workflow.internal.query.model.GetUpdateProductVariantWorkflowQuery;
import com.grab.app.workflow.internal.query.model.GetUpdateSellableProductWorkflowQuery;
import com.grab.app.workflow.port.message.WorkflowExecutionPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WorkflowExecutionPortAdapter implements WorkflowExecutionPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public WorkflowExecutionPortAdapter(
            @Qualifier("workflowCommandBus") CommandBus commandBus,
            @Qualifier("workflowQueryBus") QueryBus queryBus
    ) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @Override
    public CreateSellableProductResult startCreateSellableProduct(CreateSellableProductMessage message) {
        return commandBus.dispatch(new StartCreateSellableProductWorkflowCommand(message));
    }

    @Override
    public CreateSellableProductResult getCreateSellableProduct(Id workflowId) {
        return queryBus.dispatch(new GetCreateSellableProductWorkflowQuery(workflowId));
    }

    @Override
    public UpdateSellableProductResult startUpdateSellableProduct(UpdateSellableProductMessage message) {
        return commandBus.dispatch(new StartUpdateSellableProductWorkflowCommand(message));
    }

    @Override
    public UpdateSellableProductResult getUpdateSellableProduct(Id workflowId) {
        return queryBus.dispatch(new GetUpdateSellableProductWorkflowQuery(workflowId));
    }

    @Override
    public UpdateProductVariantResult startUpdateProductVariant(UpdateProductVariantMessage message) {
        return commandBus.dispatch(new StartUpdateProductVariantWorkflowCommand(message));
    }

    @Override
    public UpdateProductVariantResult getUpdateProductVariant(Id workflowId) {
        return queryBus.dispatch(new GetUpdateProductVariantWorkflowQuery(workflowId));
    }
}
