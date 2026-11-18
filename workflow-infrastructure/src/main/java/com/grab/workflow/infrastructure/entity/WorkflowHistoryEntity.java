package com.grab.workflow.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "workflow_history")
public class WorkflowHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Setter
    @Column(name = "node_id", length = 128)
    private String nodeId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected WorkflowHistoryEntity() {
    }

    public static WorkflowHistoryEntity empty() {
        return new WorkflowHistoryEntity();
    }

    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public void setEventType(String eventType) { this.eventType = eventType; }

    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }

    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
