package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ClaimFixture extends JpaOutboxEventEntity {

    @Lob
    @Column(name = "headers", nullable = false)
    private String headers;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    protected ClaimFixture() {
    }

    public static ClaimFixture pending(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant now
    ) {
        ClaimFixture event = new ClaimFixture();
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
