package com.saleschannel.adapter.persistence.outbox;

import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.jpa.JpaOutboxEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_record", indexes = {
        @Index(name = "idx_sales_channel_outbox_status_available", columnList = "status, available_at"),
        @Index(name = "idx_sales_channel_outbox_claimed_at", columnList = "claimed_at"),
        @Index(name = "idx_sales_channel_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
public class SalesChannelOutboxEvent extends JpaOutboxEventEntity {

    @Column(name = "headers", columnDefinition = "text", nullable = false)
    private String headers;

    @Column(name = "payload", columnDefinition = "text", nullable = false)
    private String payload;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    protected SalesChannelOutboxEvent() {
    }

    public static SalesChannelOutboxEvent pending(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant now
    ) {
        SalesChannelOutboxEvent event = new SalesChannelOutboxEvent();
        event.initialize(eventId, aggregateType, aggregateId, serializedEvent, now);
        return event;
    }

    @Override
    public String getHeaders() {
        return headers;
    }

    @Override
    protected void setHeaders(String headers) {
        this.headers = headers;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    protected void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    protected void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
