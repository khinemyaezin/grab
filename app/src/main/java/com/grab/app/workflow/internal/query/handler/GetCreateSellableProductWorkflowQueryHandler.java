package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.GetCreateSellableProductWorkflowQuery;
import com.grab.app.workflow.internal.workflows.createsellableproduct.service.CreateSellableProductWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCreateSellableProductWorkflowQueryHandler
        implements QueryHandler<GetCreateSellableProductWorkflowQuery, CreateSellableProductResult> {

    private final CreateSellableProductWorkflowService service;

    public GetCreateSellableProductWorkflowQueryHandler(CreateSellableProductWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowReadTransactional
    public CreateSellableProductResult handle(GetCreateSellableProductWorkflowQuery query) {
        return service.get(query.workflowId()).orElse(null);
    }

    @Override
    public Class<GetCreateSellableProductWorkflowQuery> getQueryType() {
        return GetCreateSellableProductWorkflowQuery.class;
    }
}

