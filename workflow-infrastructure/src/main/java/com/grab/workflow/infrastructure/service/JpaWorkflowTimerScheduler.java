package com.grab.workflow.infrastructure.service;

import com.grab.framework.workflow.WorkflowTimerIntent;
import com.grab.framework.workflow.WorkflowTimerScheduler;
import com.grab.framework.workflow.WorkflowTimerClaim;
import com.grab.framework.workflow.WorkflowTimerClaimPort;
import com.grab.framework.workflow.WorkflowTimer;
import com.grab.framework.workflow.TimerPurpose;
import com.grab.workflow.infrastructure.entity.WorkflowTimerStatus;
import com.grab.workflow.infrastructure.entity.WorkflowTimerEntity;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowTimerJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.Clock;
import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JpaWorkflowTimerScheduler implements WorkflowTimerScheduler, WorkflowTimerClaimPort {

    private final WorkflowTimerJpaRepository repository;

    public JpaWorkflowTimerScheduler(WorkflowTimerJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public void schedule(String executionId, List<WorkflowTimerIntent> timers) {
        Objects.requireNonNull(executionId, "executionId");
        Objects.requireNonNull(timers, "timers");
        for (WorkflowTimerIntent timer : timers) {
            boolean exists = repository.existsByExecutionIdAndNodeIdAndNodeGenerationAndPurposeAndPhase(
                    executionId,
                    timer.nodeId(),
                    timer.generation(),
                    timer.purpose().name(),
                    timer.phase()
            );
            if (exists) {
                continue;
            }
            WorkflowTimerEntity entity = WorkflowTimerEntity.scheduled();
            entity.setExecutionId(executionId);
            entity.setNodeId(timer.nodeId());
            entity.setNodeGeneration(timer.generation());
            entity.setLogicalOperationId(timer.logicalOperationId());
            entity.setPhase(timer.phase());
            entity.setPurpose(timer.purpose().name());
            entity.setDueAt(timer.dueAt());
            entity.setStatus(WorkflowTimerStatus.SCHEDULED);
            repository.save(entity);
        }
        repository.flush();
    }

    @Override
    public List<WorkflowTimerClaim> claimDue(Instant now, int limit, Duration lease) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(lease, "lease");
        if (lease.isNegative() || lease.isZero()) {
            throw new IllegalArgumentException("lease must be positive");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<WorkflowTimerEntity> scheduled = repository.findByStatusAndDueAtLessThanEqualOrderByDueAtAsc(
                WorkflowTimerStatus.SCHEDULED,
                now,
                pageRequest
        );
        List<WorkflowTimerEntity> entities = new ArrayList<>(scheduled);
        if (entities.size() < limit) {
            Pageable remaining = PageRequest.of(0, limit - entities.size());
            entities.addAll(repository.findByStatusAndLeaseExpiresAtLessThanEqualOrderByDueAtAsc(
                    WorkflowTimerStatus.CLAIMED,
                    now,
                    remaining
            ));
        }
        Instant leaseExpiresAt = now.plus(lease);
        List<WorkflowTimerClaim> claims = entities.stream()
                .map(entity -> {
                    String claimToken = UUID.randomUUID().toString();
                    entity.setStatus(WorkflowTimerStatus.CLAIMED);
                    entity.setClaimedAt(now);
                    entity.setClaimToken(claimToken);
                    entity.setLeaseExpiresAt(leaseExpiresAt);
                    int generation = Math.toIntExact(entity.getNodeGeneration());
                    WorkflowTimer timer = new WorkflowTimer(
                            entity.getNodeId(),
                            entity.getLogicalOperationId(),
                            TimerPurpose.valueOf(entity.getPurpose()),
                            generation,
                            entity.getDueAt(),
                            entity.getPhase()
                    );
                    return new WorkflowTimerClaim(entity.getId(), entity.getExecutionId(), timer, claimToken, leaseExpiresAt);
                })
                .toList();
        repository.flush();
        return claims;
    }

    @Override
    public boolean isCurrent(WorkflowTimerClaim claim, Clock clock) {
        Objects.requireNonNull(claim, "claim");
        return repository.findByIdForUpdate(claim.timerId())
                .map(entity -> entity.getStatus() == WorkflowTimerStatus.CLAIMED
                        && Objects.equals(entity.getClaimToken(), claim.claimToken())
                        && entity.getLeaseExpiresAt() != null
                        && clock.instant().isBefore(entity.getLeaseExpiresAt()))
                .orElse(false);
    }

    @Override
    public void markFired(WorkflowTimerClaim claim) {
        WorkflowTimerEntity entity = repository.findByIdForUpdate(claim.timerId()).orElse(null);
        if (entity == null || entity.getStatus() != WorkflowTimerStatus.CLAIMED
                || !Objects.equals(entity.getClaimToken(), claim.claimToken())) {
            return;
        }
        entity.setStatus(WorkflowTimerStatus.FIRED);
        entity.setClaimToken(null);
        entity.setLeaseExpiresAt(null);
        repository.flush();
    }
}
