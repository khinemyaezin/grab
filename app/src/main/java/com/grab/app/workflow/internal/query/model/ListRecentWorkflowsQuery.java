package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.WorkflowExecutionResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListRecentWorkflowsQuery(
        int page,
        int size
) implements Query<List<WorkflowExecutionResult>> {
}

