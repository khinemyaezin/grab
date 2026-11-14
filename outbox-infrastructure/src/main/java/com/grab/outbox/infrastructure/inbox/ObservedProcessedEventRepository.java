package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Counts {@code inbox.duplicate} (tag {@code consumer}) when a redelivered event is dropped.
 */
public final class ObservedProcessedEventRepository implements ProcessedEventRepository {

    private final ProcessedEventRepository delegate;
    private final Consumer<String> onDuplicate;

    public ObservedProcessedEventRepository(ProcessedEventRepository delegate, Consumer<String> onDuplicate) {
        this.delegate = delegate;
        this.onDuplicate = onDuplicate;
    }

    public static ProcessedEventRepository withMetrics(ProcessedEventRepository delegate,
                                                       ObjectProvider<MeterRegistry> meters) {
        return new ObservedProcessedEventRepository(delegate, consumer -> meters.ifAvailable(registry -> {
            Counter duplicate = registry.counter("inbox.duplicate", "consumer", consumer);
            duplicate.increment();
        }));
    }

    @Override
    public boolean markProcessed(String consumer, UUID eventId) {
        boolean first = delegate.markProcessed(consumer, eventId);
        if (!first) {
            onDuplicate.accept(consumer);
        }
        return first;
    }

    @Override
    public int deleteProcessedBefore(Instant cutoff, int limit) {
        return delegate.deleteProcessedBefore(cutoff, limit);
    }
}
