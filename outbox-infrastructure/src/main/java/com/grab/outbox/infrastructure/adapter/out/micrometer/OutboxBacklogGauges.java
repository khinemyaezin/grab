package com.grab.outbox.infrastructure.adapter.out.micrometer;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.core.port.in.OutboxBacklog;
import com.grab.outbox.infrastructure.core.port.out.OutboxMaintenanceStore;
import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.LongSupplier;

/**
 * Per-module backlog gauges. The backlog is table-wide, so every replica reports the same value; alert on
 * {@code max by (module)}. Each module is queried at most once per {@code cacheTtl} regardless of scrape rate.
 */
public final class OutboxBacklogGauges implements MeterBinder {

    private static final Logger log = Loggers.getLogger(OutboxBacklogGauges.class);

    private final String module;
    private final OutboxMaintenanceStore<?, ?> store;
    private final OutboxTransactions transactions;
    private final LongSupplier hotQueueDropped;
    private final Duration cacheTtl;
    private final Clock clock;

    public OutboxBacklogGauges(String module, OutboxMaintenanceStore<?, ?> store, OutboxTransactions transactions,
                               LongSupplier hotQueueDropped, Duration cacheTtl, Clock clock) {
        this.module = module;
        this.store = store;
        this.transactions = transactions;
        this.hotQueueDropped = hotQueueDropped;
        this.cacheTtl = cacheTtl;
        this.clock = clock;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        CachedBacklog backlog = new CachedBacklog();
        Gauge.builder("outbox.pending", backlog, CachedBacklog::pending)
                .tag("module", module)
                .description("Outbox rows in NEW, FAILED or PROCESSING")
                .strongReference(true)
                .register(registry);
        Gauge.builder("outbox.oldest_pending_age_seconds", backlog, CachedBacklog::oldestAgeSeconds)
                .tag("module", module)
                .description("Age of the oldest undelivered outbox row")
                .strongReference(true)
                .register(registry);
        FunctionCounter.builder("outbox.hot_queue.dropped", hotQueueDropped, LongSupplier::getAsLong)
                .tag("module", module)
                .description("Hot-queue offers rejected because the queue was full; the poller recovers them")
                .register(registry);
    }

    private final class CachedBacklog {

        private OutboxBacklog value;
        private Instant sampledAt;

        double pending() {
            OutboxBacklog backlog = sample();
            return backlog == null ? Double.NaN : backlog.pending();
        }

        double oldestAgeSeconds() {
            OutboxBacklog backlog = sample();
            if (backlog == null) {
                return Double.NaN;
            }
            Instant now = clock.instant();
            Duration age = backlog.oldestAge(now);
            return age.toMillis() / 1000.0;
        }

        private synchronized OutboxBacklog sample() {
            Instant now = clock.instant();
            boolean fresh = sampledAt != null && sampledAt.plus(cacheTtl).isAfter(now);
            if (fresh) {
                return value;
            }
            sampledAt = now;
            try {
                value = transactions.readOnly(store::pendingBacklog);
            } catch (RuntimeException exception) {
                log.warn("Could not sample {} outbox backlog", module, exception);
                value = null;
            }
            return value;
        }
    }
}
