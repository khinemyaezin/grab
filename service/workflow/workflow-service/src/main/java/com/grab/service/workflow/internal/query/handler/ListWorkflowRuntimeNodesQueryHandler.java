package com.grab.service.workflow.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowNodeView;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort;
import com.grab.service.workflow.internal.config.WorkflowReadTransactional;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeNodesQuery;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListWorkflowRuntimeNodesQueryHandler
        implements QueryHandler<ListWorkflowRuntimeNodesQuery, List<WorkflowRuntimeQueryPort.WorkflowNodeView>> {

    private final WorkflowQueryStore queryStore;

    public ListWorkflowRuntimeNodesQueryHandler(WorkflowQueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @Override
    @WorkflowReadTransactional
    public List<WorkflowRuntimeQueryPort.WorkflowNodeView> handle(ListWorkflowRuntimeNodesQuery query) {
        return queryStore.listNodes(query.executionId().getValue(), query.callerScope()).stream()
                .map(this::toView)
                .toList();
    }

    private WorkflowRuntimeQueryPort.WorkflowNodeView toView(WorkflowNodeView node) {
        return new WorkflowRuntimeQueryPort.WorkflowNodeView(
                new CommonId(node.executionId()),
                node.nodeId(),
                node.branchId(),
                node.status(),
                node.attempts(),
                node.deadlineAt(),
                node.retryAt(),
                node.errorMessage()
        );
    }

    @Override
    public Class<ListWorkflowRuntimeNodesQuery> getQueryType() {
        return ListWorkflowRuntimeNodesQuery.class;
    }
}
