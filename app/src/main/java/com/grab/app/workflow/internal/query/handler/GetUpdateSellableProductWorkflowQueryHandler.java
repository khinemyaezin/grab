package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.GetUpdateSellableProductWorkflowQuery;
import com.grab.app.workflow.internal.workflows.updatesellableproduct.service.UpdateSellableProductWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetUpdateSellableProductWorkflowQueryHandler
        implements QueryHandler<GetUpdateSellableProductWorkflowQuery, UpdateSellableProductResult> {

    private final UpdateSellableProductWorkflowService service;

    public GetUpdateSellableProductWorkflowQueryHandler(UpdateSellableProductWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowReadTransactional
    public UpdateSellableProductResult handle(GetUpdateSellableProductWorkflowQuery query) {
        return service.get(query.workflowId()).orElse(null);
    }

    @Override
    public Class<GetUpdateSellableProductWorkflowQuery> getQueryType() {
        return GetUpdateSellableProductWorkflowQuery.class;
    }
}

