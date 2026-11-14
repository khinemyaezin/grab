package com.grab.app.shared.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxBacklog;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Per-module backlog gauges. The backlog is table-wide, so every replica reports the same value; alert on
 * {@code max by (module)}. Each module is queried at most once per {@code cacheTtl} regardless of scrape rate.
 */
public class OutboxBacklogMetrics implements MeterBinder {

    private static final Logger log = Loggers.getLogger(OutboxBacklogMetrics.class);

    private final List<AbstractOutboxProcessor<?, ?>> processors;
    private final Duration cacheTtl;
    private final Clock clock;

    public OutboxBacklogMetrics(List<AbstractOutboxProcessor<?, ?>> processors, Duration cacheTtl, Clock clock) {
        this.processors = List.copyOf(processors);
        this.cacheTtl = cacheTtl;
        this.clock = clock;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (AbstractOutboxProcessor<?, ?> processor : processors) {
            String module = processor.module();
            CachedBacklog backlog = new CachedBacklog(processor);
            Gauge.builder("outbox.pending", backlog, CachedBacklog::pending)
                    .tag("module", module)
                    .description("Outbox rows in NEW, FAILED or PROCESSING")
                    .register(registry);
            Gauge.builder("outbox.oldest_pending_age_seconds", backlog, CachedBacklog::oldestAgeSeconds)
                    .tag("module", module)
                    .description("Age of the oldest undelivered outbox row")
                    .register(registry);
            FunctionCounter.builder("outbox.hot_queue.dropped", processor, AbstractOutboxProcessor::hotQueueDropped)
                    .tag("module", module)
                    .description("Hot-queue offers rejected because the queue was full; the poller recovers them")
                    .register(registry);
        }
    }

    private final class CachedBacklog {

        private final AbstractOutboxProcessor<?, ?> processor;
        private OutboxBacklog value;
        private Instant sampledAt;

        private CachedBacklog(AbstractOutboxProcessor<?, ?> processor) {
            this.processor = processor;
        }

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
                value = processor.backlog();
            } catch (RuntimeException exception) {
                log.warn("Could not sample {} outbox backlog", processor.module(), exception);
                value = null;
            }
            return value;
        }
    }
}
