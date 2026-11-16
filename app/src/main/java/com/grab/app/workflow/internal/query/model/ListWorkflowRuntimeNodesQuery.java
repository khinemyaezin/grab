package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record ListWorkflowRuntimeNodesQuery(
        Id executionId,
        String callerScope
) implements Query<List<WorkflowRuntimeQueryPort.WorkflowNodeView>> {
}
