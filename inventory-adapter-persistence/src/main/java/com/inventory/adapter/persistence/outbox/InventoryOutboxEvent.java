package com.inventory.adapter.persistence.outbox;

import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_record", indexes = {
        @Index(name = "idx_inventory_outbox_status_available", columnList = "status, available_at"),
        @Index(name = "idx_inventory_outbox_claimed_at", columnList = "claimed_at"),
        @Index(name = "idx_inventory_outbox_aggregate", columnList = "aggregate_type, aggregate_id"),
        @Index(name = "idx_inventory_outbox_published_at", columnList = "published_at")
})
public class InventoryOutboxEvent extends JpaOutboxEventEntity {

    @Lob
    @Column(name = "headers", nullable = false)
    private String headers;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    protected InventoryOutboxEvent() {
    }

    public static InventoryOutboxEvent pending(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant now
    ) {
        InventoryOutboxEvent event = new InventoryOutboxEvent();
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
