package com.grab.service.workflow.internal.query.model;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowHistoryEntry;

import java.util.List;

public record ListWorkflowRuntimeHistoryQuery(
        Id executionId,
        String callerScope,
        int page,
        int size
) implements Query<List<WorkflowHistoryEntry>> {
}
