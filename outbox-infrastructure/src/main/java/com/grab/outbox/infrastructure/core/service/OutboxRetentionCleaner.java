package com.grab.outbox.infrastructure.core.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.core.port.out.OutboxMaintenanceStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public final class OutboxRetentionCleaner {

    private static final Logger log = Loggers.getLogger(OutboxRetentionCleaner.class);

    private final String module;
    private final OutboxMaintenanceStore<?, ?> store;
    private final OutboxTransactions transactions;
    private final OutboxObserver observer;
    private final Duration retention;
    private final int batchSize;
    private final Clock clock;

    public OutboxRetentionCleaner(String module, OutboxMaintenanceStore<?, ?> store, OutboxTransactions transactions,
                                  OutboxObserver observer, Duration retention, int batchSize, Clock clock) {
        this.module = module;
        this.store = store;
        this.transactions = transactions;
        this.observer = observer;
        this.retention = retention;
        this.batchSize = batchSize;
        this.clock = clock;
    }

    public void cleanupPublished() {
        Instant cutoff = clock.instant().minus(retention);
        int total = 0;
        int deleted;
        do {
            deleted = transactions.inNewTransaction(() -> store.deletePublishedOlderThan(cutoff, batchSize));
            total += deleted;
        } while (deleted >= batchSize);
        if (total > 0) {
            log.info("Deleted {} published {} outbox row(s) older than {}", total, module, cutoff);
            observer.cleanedUp(module, total);
        }
    }
}
