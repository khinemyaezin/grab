package com.grab.app.workflow.internal.query.handler;

import com.grab.app.workflow.internal.config.WorkflowReadTransactional;
import com.grab.app.workflow.internal.query.model.GetWorkflowRuntimeExecutionQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import com.grab.framework.workflow.WorkflowQueryStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetWorkflowRuntimeExecutionQueryHandler
        implements QueryHandler<GetWorkflowRuntimeExecutionQuery, Optional<WorkflowRuntimeQueryPort.WorkflowExecutionSummary>> {

    private final WorkflowQueryStore queryStore;

    public GetWorkflowRuntimeExecutionQueryHandler(WorkflowQueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @Override
    @WorkflowReadTransactional
    public Optional<WorkflowRuntimeQueryPort.WorkflowExecutionSummary> handle(GetWorkflowRuntimeExecutionQuery query) {
        return queryStore.findExecution(query.executionId().getValue(), query.callerScope())
                .map(this::toSummary);
    }

    private WorkflowRuntimeQueryPort.WorkflowExecutionSummary toSummary(WorkflowExecutionMetadata metadata) {
        return new WorkflowRuntimeQueryPort.WorkflowExecutionSummary(
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
