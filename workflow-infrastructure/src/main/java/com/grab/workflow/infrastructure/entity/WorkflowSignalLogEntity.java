package com.grab.workflow.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "workflow_signal_log")
@IdClass(WorkflowSignalLogEntity.Pk.class)
public class WorkflowSignalLogEntity {

    @Id
    @Column(name = "workflow_id", nullable = false, length = 64)
    private String workflowId;

    @Id
    @Column(name = "step", nullable = false, length = 128)
    private String step;

    @Id
    @Column(name = "dedup_key", nullable = false, length = 256)
    private String dedupKey;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public WorkflowSignalLogEntity() {
    }

    public WorkflowSignalLogEntity(String workflowId, String step, String dedupKey, Instant recordedAt) {
        this.workflowId = workflowId;
        this.step = step;
        this.dedupKey = dedupKey;
        this.recordedAt = recordedAt;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getStep() {
        return step;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public static final class Pk implements Serializable {
        private String workflowId;
        private String step;
        private String dedupKey;

        public Pk() {
        }

        public Pk(String workflowId, String step, String dedupKey) {
            this.workflowId = workflowId;
            this.step = step;
            this.dedupKey = dedupKey;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(workflowId, pk.workflowId)
                    && Objects.equals(step, pk.step)
                    && Objects.equals(dedupKey, pk.dedupKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workflowId, step, dedupKey);
        }
    }
}
