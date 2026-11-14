package com.inventory.adapter.persistence.outbox;

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
public class InventoryProcessedEvent extends JpaProcessedEventEntity {

    protected InventoryProcessedEvent() {
    }

    public static InventoryProcessedEvent of(ProcessedEventKey key, Instant processedAt) {
        InventoryProcessedEvent row = new InventoryProcessedEvent();
        row.initialize(key, processedAt);
        return row;
    }
}
