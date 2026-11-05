package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.adapter.inbound.message.mapper.GetWorkflowStatusPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.ListRecentWorkflowsPortMapper;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.WorkflowExecutionResult;
import com.grab.app.workflow.port.message.WorkflowQueryPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class WorkflowQueryPortAdapter implements WorkflowQueryPort {

    private final QueryBus queryBus;
    private final GetWorkflowStatusPortMapper getWorkflowStatusMapper;
    private final ListRecentWorkflowsPortMapper listRecentWorkflowsMapper;

    public WorkflowQueryPortAdapter(
            @Qualifier("workflowQueryBus") QueryBus queryBus,
            GetWorkflowStatusPortMapper getWorkflowStatusMapper,
            ListRecentWorkflowsPortMapper listRecentWorkflowsMapper) {
        this.queryBus = queryBus;
        this.getWorkflowStatusMapper = getWorkflowStatusMapper;
        this.listRecentWorkflowsMapper = listRecentWorkflowsMapper;
    }

    @Override
    public Optional<WorkflowExecutionResult> getWorkflowStatus(Id workflowId) {
        return Optional.empty();
    }

    @Override
    public List<WorkflowExecutionResult> listRecentWorkflows(int page, int size) {
        return Collections.emptyList();
    }
}
