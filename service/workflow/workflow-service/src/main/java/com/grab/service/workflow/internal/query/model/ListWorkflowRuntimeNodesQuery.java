package com.grab.service.workflow.internal.query.model;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowNodeView;

import java.util.List;

public record ListWorkflowRuntimeNodesQuery(
        Id executionId,
        String callerScope
) implements Query<List<WorkflowNodeView>> {
}
