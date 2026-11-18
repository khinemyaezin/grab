package com.grab.workflow.infrastructure.adapter;

import com.grab.workflow.infrastructure.repository.jpa.WorkflowExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowNodeExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowHistoryJpaRepository;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import com.grab.framework.workflow.WorkflowHistoryEntry;
import com.grab.framework.workflow.WorkflowIncidentView;
import com.grab.framework.workflow.WorkflowNodeView;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.workflow.infrastructure.entity.WorkflowExecutionEntity;
import com.grab.workflow.infrastructure.entity.WorkflowHistoryEntity;
import com.grab.workflow.infrastructure.entity.WorkflowNodeExecutionEntity;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class JpaWorkflowQueryStore implements WorkflowQueryStore {

    private static final List<ExecutionStatus> INCIDENT_STATUSES = List.of(
            ExecutionStatus.NEEDS_ATTENTION,
            ExecutionStatus.FAILED
    );

    private final WorkflowExecutionJpaRepository executionRepository;
    private final WorkflowNodeExecutionJpaRepository nodeRepository;
    private final WorkflowHistoryJpaRepository historyRepository;

    public JpaWorkflowQueryStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeRepository,
            WorkflowHistoryJpaRepository historyRepository
    ) {
        this.executionRepository = Objects.requireNonNull(executionRepository, "executionRepository");
        this.nodeRepository = Objects.requireNonNull(nodeRepository, "nodeRepository");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository");
    }

    @Override
    public Optional<WorkflowExecutionMetadata> findExecution(String executionId, String callerScope) {
        return executionRepository.findById(executionId)
                .filter(entity -> entity.getCallerScope().equals(callerScope))
                .map(this::toMetadata);
    }

    @Override
    public List<WorkflowNodeView> listNodes(String executionId, String callerScope) {
        if (findExecution(executionId, callerScope).isEmpty()) {
            return List.of();
        }
        return nodeRepository.findByExecutionIdOrderByNodeIdAsc(executionId).stream()
                .map(entity -> new WorkflowNodeView(
                        executionId,
                        entity.getNodeId(),
                        entity.getBranchId(),
                        entity.getStatus().name(),
                        entity.getAttempts(),
                        entity.getDeadlineAt(),
                        entity.getRetryAt(),
                        entity.getErrorMessage()
                ))
                .toList();
    }

    @Override
    public List<WorkflowHistoryEntry> listHistory(String executionId, String callerScope, int page, int size) {
        if (findExecution(executionId, callerScope).isEmpty()) {
            return List.of();
        }
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.clamp(size, 1, 200);
        return historyRepository.findByExecutionIdOrderBySequenceNumberAsc(executionId, PageRequest.of(pageNumber, pageSize)).stream()
                .map(this::toHistory)
                .toList();
    }

    @Override
    public List<WorkflowIncidentView> listIncidents(String callerScope, int page, int size) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.clamp(size, 1, 200);
        PageRequest request = PageRequest.of(pageNumber, pageSize);
        return executionRepository.findByCallerScopeAndStatusInOrderByUpdatedAtAscIdAsc(
                        callerScope,
                        INCIDENT_STATUSES,
                        request
                ).stream()
                .map(entity -> new WorkflowIncidentView(
                        entity.getId(),
                        entity.getDefinitionName(),
                        null,
                        entity.getStatus().name(),
                        entity.getFailureReason() == null ? entity.getErrorMessage() : entity.getFailureReason(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }

    private WorkflowExecutionMetadata toMetadata(WorkflowExecutionEntity entity) {
        return new WorkflowExecutionMetadata(
                entity.getId(),
                entity.getDefinitionName(),
                entity.getDefinitionVersion(),
                entity.getPayloadVersion(),
                entity.getCallerScope(),
                entity.getStatus(),
                entity.isCancellationRequested(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private WorkflowHistoryEntry toHistory(WorkflowHistoryEntity entity) {
        return new WorkflowHistoryEntry(
                entity.getExecutionId(),
                entity.getSequenceNumber(),
                entity.getNodeId(),
                entity.getEventType(),
                entity.getDetailsJson(),
                entity.getOccurredAt()
        );
    }
}
