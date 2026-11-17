package com.grab.service.workflow.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowExecutionSummary;
import com.grab.service.workflow.internal.config.WorkflowReadTransactional;
import com.grab.service.workflow.internal.query.model.GetWorkflowRuntimeExecutionQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetWorkflowRuntimeExecutionQueryHandler
        implements QueryHandler<GetWorkflowRuntimeExecutionQuery, Optional<WorkflowExecutionSummary>> {

    private final WorkflowQueryStore queryStore;

    public GetWorkflowRuntimeExecutionQueryHandler(WorkflowQueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @Override
    @WorkflowReadTransactional
    public Optional<WorkflowExecutionSummary> handle(GetWorkflowRuntimeExecutionQuery query) {
        return queryStore.findExecution(query.executionId().getValue(), query.callerScope())
                .map(this::toSummary);
    }

    private WorkflowExecutionSummary toSummary(WorkflowExecutionMetadata metadata) {
        return new WorkflowExecutionSummary(
                new CommonId(metadata.executionId()),
                metadata.definitionName(),
                metadata.definitionVersion(),
                metadata.status().name(),
                metadata.cancellationRequested(),
                metadata.createdAt(),
                metadata.updatedAt()
        );
    }

    @Override
    public Class<GetWorkflowRuntimeExecutionQuery> getQueryType() {
        return GetWorkflowRuntimeExecutionQuery.class;
    }
}
