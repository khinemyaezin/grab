package com.grab.app.shared.outbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.UUID;

/**
 * Counts {@code inbox.duplicate} (tag {@code consumer}) when a redelivered event is dropped.
 */
final class ObservedProcessedEventRepository implements ProcessedEventRepository {

    private final ProcessedEventRepository delegate;
    private final ObjectProvider<MeterRegistry> meterRegistry;

    ObservedProcessedEventRepository(ProcessedEventRepository delegate, ObjectProvider<MeterRegistry> meterRegistry) {
        this.delegate = delegate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean markProcessed(String consumer, UUID eventId) {
        boolean first = delegate.markProcessed(consumer, eventId);
        if (!first) {
            meterRegistry.ifAvailable(registry -> registry.counter("inbox.duplicate", "consumer", consumer).increment());
        }
        return first;
    }

    @Override
    public int deleteProcessedBefore(Instant cutoff, int limit) {
        return delegate.deleteProcessedBefore(cutoff, limit);
    }
}
