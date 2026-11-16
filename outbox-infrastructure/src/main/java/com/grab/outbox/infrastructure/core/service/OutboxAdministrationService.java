package com.grab.outbox.infrastructure.core.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.outbox.infrastructure.core.port.in.OutboxAdministrationPort;
import com.grab.outbox.infrastructure.core.port.in.OutboxBacklog;
import com.grab.outbox.infrastructure.core.port.out.OutboxMaintenanceStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public final class OutboxAdministrationService<T extends OutboxEntry<Long>> implements OutboxAdministrationPort {

    private static final Logger log = Loggers.getLogger(OutboxAdministrationService.class);
    private static final int MAX_LIMIT = 1000;

    private final String module;
    private final OutboxMaintenanceStore<T, Long> store;
    private final OutboxTransactions transactions;
    private final Clock clock;

    public OutboxAdministrationService(String module, OutboxMaintenanceStore<T, Long> store,
                                       OutboxTransactions transactions, Clock clock) {
        this.module = module;
        this.store = store;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public int replayDead(long fromId, long toId, int limit) {
        if (fromId > toId) {
            throw new IllegalArgumentException("fromId must be <= toId");
        }
        int boundedLimit = Math.clamp(limit, 1, MAX_LIMIT);
        int count = transactions.inNewTransaction(() -> {
            List<T> rows = store.findDeadForReplay(fromId, toId, boundedLimit);
            Instant now = clock.instant();
            for (T row : rows) {
                row.requeue(now);
            }
            return rows.size();
        });
        log.warn("Operator replay requeued {} dead {} outbox row(s) in id range [{}, {}]",
                count, module, fromId, toId);
        return count;
    }

    @Override
    public OutboxBacklog backlog() {
        return transactions.readOnly(store::pendingBacklog);
    }
}
