package com.grab.discovery.adapter.persistence.outbox;

import com.grab.outbox.infrastructure.inbox.jpa.JpaProcessedEventEntity;
import com.grab.outbox.infrastructure.inbox.jpa.ProcessedEventKey;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "processed_event", indexes = {
        @Index(name = "idx_processed_event_processed_at", columnList = "processed_at")
})
public class DiscoveryProcessedEvent extends JpaProcessedEventEntity {

    protected DiscoveryProcessedEvent() {
    }

    public static DiscoveryProcessedEvent of(ProcessedEventKey key, Instant processedAt) {
        DiscoveryProcessedEvent row = new DiscoveryProcessedEvent();
        row.initialize(key, processedAt);
        return row;
    }
}
