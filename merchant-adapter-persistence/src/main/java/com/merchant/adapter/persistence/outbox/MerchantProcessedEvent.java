package com.merchant.adapter.persistence.outbox;

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
public class MerchantProcessedEvent extends JpaProcessedEventEntity {

    protected MerchantProcessedEvent() {
    }

    public static MerchantProcessedEvent of(ProcessedEventKey key, Instant processedAt) {
        MerchantProcessedEvent row = new MerchantProcessedEvent();
        row.initialize(key, processedAt);
        return row;
    }
}
