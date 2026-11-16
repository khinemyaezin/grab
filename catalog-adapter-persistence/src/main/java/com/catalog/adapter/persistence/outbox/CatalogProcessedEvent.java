package com.catalog.adapter.persistence.outbox;

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
public class CatalogProcessedEvent extends JpaProcessedEventEntity {

    protected CatalogProcessedEvent() {
    }

    public static CatalogProcessedEvent of(ProcessedEventKey key, Instant processedAt) {
        CatalogProcessedEvent row = new CatalogProcessedEvent();
        row.initialize(key, processedAt);
        return row;
    }
}
