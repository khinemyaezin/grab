package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowOptimisticLockException;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.workflow.infrastructure.entity.WorkflowCorrelationEntity;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JpaWorkflowStore implements WorkflowStore {

    private final WorkflowInstanceJpaRepository repository;
    private final WorkflowInstanceMapper mapper;
    private final WorkflowCorrelationJpaRepository correlationRepository;
    private final WorkflowSignalLogJpaRepository signalLogRepository;

    public JpaWorkflowStore(
            WorkflowInstanceJpaRepository repository,
            WorkflowInstanceMapper mapper,
            WorkflowCorrelationJpaRepository correlationRepository,
            WorkflowSignalLogJpaRepository signalLogRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.correlationRepository = correlationRepository;
        this.signalLogRepository = signalLogRepository;
    }

    /**
     * Persists the instance under the optimistic lock held by {@code workflow_instance.version}.
     *
     * <p>Flushes eagerly so a concurrent writer is detected here, inside the caller's retry scope,
     * rather than at transaction commit where nothing can react to it. The JPA failure is
     * translated to {@link WorkflowOptimisticLockException} so the engine never sees a Spring or
     * Hibernate type.
     */
    @Override
    public WorkflowInstance save(WorkflowInstance instance) {
        Objects.requireNonNull(instance, "instance");
        WorkflowInstanceEntity entity = repository.findById(instance.id())
                .orElseGet(WorkflowInstanceEntity::new);
        mapper.toEntity(instance, entity);
        WorkflowInstanceEntity saved;
        try {
            saved = repository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException exception) {
            throw new WorkflowOptimisticLockException(instance.id());
        }
        instance.assignVersion(saved.getVersion() == null ? 0L : saved.getVersion());
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WorkflowInstance> findById(String workflowId) {
        return repository.findById(workflowId).map(mapper::toDomain);
    }

    @Override
    public Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey) {
        return repository.findByWorkflowNameAndIdempotencyKey(workflowName, idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(status, "status");
        return repository.findByWorkflowNameAndStatus(workflowName, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkflowInstance> findByCorrelationKey(String workflowName, String correlationKey) {
        return correlationRepository.findById(new WorkflowCorrelationEntity.Pk(workflowName, correlationKey))
                .flatMap(correlation -> findById(correlation.getWorkflowId()));
    }

    @Override
    public List<WorkflowInstance> findAllByCorrelationKey(String correlationKey) {
        Objects.requireNonNull(correlationKey, "correlationKey");
        return correlationRepository.findByCorrelationKey(correlationKey).stream()
                .map(WorkflowCorrelationEntity::getWorkflowId)
                .distinct()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public void putCorrelation(String workflowName, String correlationKey, String workflowId, String step) {
        WorkflowCorrelationEntity entity = correlationRepository
                .findById(new WorkflowCorrelationEntity.Pk(workflowName, correlationKey))
                .orElseGet(WorkflowCorrelationEntity::new);
        entity.setWorkflowName(workflowName);
        entity.setCorrelationKey(correlationKey);
        entity.setWorkflowId(workflowId);
        entity.setStep(step);
        correlationRepository.save(entity);
    }

    @Override
    public void removeCorrelationsForWorkflow(String workflowId) {
        correlationRepository.deleteByWorkflowId(workflowId);
    }

    @Override
    public boolean tryRecordSignal(String workflowId, String step, String dedupKey) {
        return signalLogRepository.claimSignal(workflowId, step, dedupKey, Instant.now()) == 1;
    }

    @Override
    public List<WorkflowInstance> findResumable(
            String workflowName,
            Collection<WorkflowStatus> statuses,
            Instant updatedBefore,
            int limit
    ) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(statuses, "statuses");
        Objects.requireNonNull(updatedBefore, "updatedBefore");
        return repository.findByWorkflowNameAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                        workflowName,
                        statuses,
                        updatedBefore,
                        PageRequest.of(0, Math.max(limit, 1))
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
