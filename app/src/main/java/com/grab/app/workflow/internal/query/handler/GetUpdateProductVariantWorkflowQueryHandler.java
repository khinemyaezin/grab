package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.GetUpdateProductVariantWorkflowQuery;
import com.grab.app.workflow.internal.workflows.updateproductvariant.service.UpdateProductVariantWorkflowService;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetUpdateProductVariantWorkflowQueryHandler
        implements QueryHandler<GetUpdateProductVariantWorkflowQuery, UpdateProductVariantResult> {

    private final UpdateProductVariantWorkflowService service;

    public GetUpdateProductVariantWorkflowQueryHandler(UpdateProductVariantWorkflowService service) {
        this.service = service;
    }

    @Override
    @WorkflowReadTransactional
    public UpdateProductVariantResult handle(GetUpdateProductVariantWorkflowQuery query) {
        return service.get(query.workflowId()).orElse(null);
    }

    @Override
    public Class<GetUpdateProductVariantWorkflowQuery> getQueryType() {
        return GetUpdateProductVariantWorkflowQuery.class;
    }
}

