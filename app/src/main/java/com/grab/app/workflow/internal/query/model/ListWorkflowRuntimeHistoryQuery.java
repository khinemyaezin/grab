package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record ListWorkflowRuntimeHistoryQuery(
        Id executionId,
        String callerScope,
        int page,
        int size
) implements Query<List<WorkflowRuntimeQueryPort.WorkflowHistoryEntry>> {
}
