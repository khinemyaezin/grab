package com.merchant.infrastructure.outbox;

import com.grab.framework.outbox.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "merchant_outbox_events", indexes = {
        @Index(name = "idx_merchant_outbox_status_available", columnList = "status, available_at"),
        @Index(name = "idx_merchant_outbox_claimed_at", columnList = "claimed_at"),
        @Index(name = "idx_merchant_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
public class MerchantOutboxEvent implements OutboxEntry<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(columnDefinition = "text", nullable = false)
    private String headers;

    @Column(columnDefinition = "text", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "claim_token")
    private String claimToken;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    public static MerchantOutboxEvent pending(String aggregateType, String aggregateId,
                                              SerializedEvent serialized, LocalDateTime now) {
        MerchantOutboxEvent event = new MerchantOutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = serialized.eventType();
        event.eventVersion = serialized.eventVersion();

        event.headers = serialized.headers();
        event.payload = serialized.payload();
        event.occurredAt = now;
        event.availableAt = now;
        return event;
    }

    public void markProcessing(LocalDateTime now, String token) {
        status = OutboxStatus.PROCESSING; claimedAt = now; claimToken = token; lastError = null; attemptCount++;
    }
    public void markPublished(LocalDateTime now) {
        status = OutboxStatus.PUBLISHED; publishedAt = now; claimedAt = null; claimToken = null; lastError = null;
    }
    public void markFailed(LocalDateTime now, String error, Duration delay) {
        status = OutboxStatus.FAILED; availableAt = now.plus(delay); claimedAt = null; claimToken = null; lastError = error;
    }
}
