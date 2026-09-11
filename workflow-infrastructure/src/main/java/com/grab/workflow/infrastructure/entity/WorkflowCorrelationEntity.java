package com.grab.workflow.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "workflow_correlation")
@IdClass(WorkflowCorrelationEntity.Pk.class)
public class WorkflowCorrelationEntity {

    @Id
    @Column(name = "workflow_name", nullable = false, length = 128)
    private String workflowName;

    @Id
    @Column(name = "correlation_key", nullable = false, length = 256)
    private String correlationKey;

    @Column(name = "workflow_id", nullable = false, length = 64)
    private String workflowId;

    @Column(name = "step", nullable = false, length = 128)
    private String step;

    public WorkflowCorrelationEntity() {
    }

    public WorkflowCorrelationEntity(String workflowName, String correlationKey, String workflowId, String step) {
        this.workflowName = workflowName;
        this.correlationKey = correlationKey;
        this.workflowId = workflowId;
        this.step = step;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public static final class Pk implements Serializable {

        private String workflowName;
        private String correlationKey;

        public Pk() {
        }

        public Pk(String workflowName, String correlationKey) {
            this.workflowName = workflowName;
            this.correlationKey = correlationKey;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(workflowName, pk.workflowName)
                    && Objects.equals(correlationKey, pk.correlationKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workflowName, correlationKey);
        }
    }
}
