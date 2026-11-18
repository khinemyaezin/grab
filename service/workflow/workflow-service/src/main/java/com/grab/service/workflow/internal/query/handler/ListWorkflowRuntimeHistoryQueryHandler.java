package com.grab.service.workflow.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowHistoryEntry;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort;
import com.grab.service.workflow.internal.config.WorkflowReadTransactional;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeHistoryQuery;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListWorkflowRuntimeHistoryQueryHandler
        implements QueryHandler<ListWorkflowRuntimeHistoryQuery, List<WorkflowRuntimeQueryPort.WorkflowHistoryEntry>> {

    private final WorkflowQueryStore queryStore;

    public ListWorkflowRuntimeHistoryQueryHandler(WorkflowQueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @Override
    @WorkflowReadTransactional
    public List<WorkflowRuntimeQueryPort.WorkflowHistoryEntry> handle(ListWorkflowRuntimeHistoryQuery query) {
        return queryStore.listHistory(
                        query.executionId().getValue(),
                        query.callerScope(),
                        query.page(),
                        query.size()
                ).stream()
                .map(this::toEntry)
                .toList();
    }

    private WorkflowRuntimeQueryPort.WorkflowHistoryEntry toEntry(WorkflowHistoryEntry entry) {
        return new WorkflowRuntimeQueryPort.WorkflowHistoryEntry(
                new CommonId(entry.executionId()),
                entry.sequenceNumber(),
                entry.nodeId(),
                entry.eventType(),
                entry.occurredAt()
        );
    }

    @Override
    public Class<ListWorkflowRuntimeHistoryQuery> getQueryType() {
        return ListWorkflowRuntimeHistoryQuery.class;
    }
}
