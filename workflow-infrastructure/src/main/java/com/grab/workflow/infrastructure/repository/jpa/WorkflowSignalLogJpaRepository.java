package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowSignalLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface WorkflowSignalLogJpaRepository
        extends JpaRepository<WorkflowSignalLogEntity, WorkflowSignalLogEntity.Pk> {

    /**
     * Claims a signal for processing, returning 1 for the first delivery and 0 for a replay.
     *
     * <p>Deliberately an upsert rather than an exists-then-insert: the check-then-act version both
     * races and, on the losing side, raises a constraint violation that leaves the caller's
     * transaction unusable even though a replay is a normal, expected outcome.
     */
    @Modifying
    @Query(
            value = """
                    INSERT INTO workflow_signal_log (workflow_id, step, dedup_key, recorded_at)
                    VALUES (:workflowId, :step, :dedupKey, :recordedAt)
                    ON CONFLICT (workflow_id, step, dedup_key) DO NOTHING
                    """,
            nativeQuery = true
    )
    int claimSignal(
            @Param("workflowId") String workflowId,
            @Param("step") String step,
            @Param("dedupKey") String dedupKey,
            @Param("recordedAt") Instant recordedAt
    );
}
